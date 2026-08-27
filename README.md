# Release Note / RM — planwith-fo-schedule

## 1. 서비스 개요

| 항목 | 내용 |
|------|------|
| 서비스명 (Compose / ECR) | `planwith-fo-schedule` |
| 레포 | `planwith_fo_schedule` |
| Spring `application.name` | `schedule-service` (yml) / Compose에서는 `planwith-fo-schedule`로 덮어씀 |
| 패키지 | `com.planwith.planwith_fo_schedule` |
| 포트 | Compose `8081` / local `18081` |
| DB | `schedule` (`schedule_db` 인프라 규칙과 혼용 주의) |
| 역할 | 여행 일정 Aggregate CRUD, AI 일정 생성·저장·수정, 항공 검색·추천·확정, AI 사용량 Outbox→Kafka |

Hexagonal Spring Boot 일정 도메인 서비스로, **일정 원장 + AI 생성 + 항공 + 사용량 리포트**까지 구현되어 있습니다.

---

## 2. 도메인 범위

### 2.1 Schedule Aggregate

- 핵심: `Schedule` + `ScheduleItem` + (선택) `ScheduleFlight` / `ScheduleFlightSegment`
- 생성자 유형: `USER` / `AI` / `OTHER`
- 아이템 유형: `MOVE` / `FOOD` / `TOUR` / `STAY` / `ACTIVITY` / `ETC`
- 교통·여행 스타일·기간·인원·비용·이미지 등 VO 관리
- soft delete (`deletedAt`)
- 회원은 `memberUuid`만 보관 (Member 서비스 HTTP 연동 없음)

### 2.2 AI Schedule

- 생성 / 재생성: OpenAI 호출, **저장 전 미영속**
- 저장: `/save`로 DB 반영 (항공 선택 시 함께 저장 가능)
- AI 수정: `/{scheduleUuid}/ai/revise` (소유권 검증 있음)
- 작업 유형: `GENERATE` / `REGENERATE` / `REVISE`
- AI 사용량 → `ai_usage_outbox` 적재 (`requestId` 멱등)

### 2.3 Flight

- 검색 / 추천 / 확정 (AviationStack)
- 추천 결과 Redis 캐시 (기본 TTL 10분)
- 확정 API는 가격 검증·DB 저장 없음 → AI 일정 저장 경로에서 영속
- 공항 위치: 코드 내 정적 맵 (제한된 도시)

### 2.4 AI Usage Report (Outbox)

- 테이블: `ai_usage_outbox` (`PENDING` → `PUBLISHED`)
- Kafka 토픽(기본): `planwith.ai-usage.reported`
- Token 등 소비 측은 `requestId`를 멱등 키로 처리해야 함
- `ai.usage-report.enabled=true`일 때만 Relay 발행 (기본 OFF)

---

## 3. API 그룹

| 구분 | Prefix |
|------|--------|
| Deploy | `/api/planwith-fo-schedule` (`deploy-check`) |
| Schedule CRUD | `/schedules`, `/api/v1/schedules` |
| Calendar | `GET .../calendar` (본인 일정만, `X-Auth-User-Id` 필수) |
| AI Schedule | `/schedules/ai`, `/api/v1/schedules/ai` (`generate` / `regenerate` / `save`) |
| AI Revise | `POST .../{scheduleUuid}/ai/revise` |
| Flight | `/flights`, `/api/v1/flights` (`search` / `recommendations` / `confirmations`) |
| Flight Location | `/flight-locations`, `/api/v1/flight-locations` |

AI·항공 일부 API는 `X-Member-UUID` 헤더 사용.

---

## 4. 외부 연동

| 시스템 | 내용 |
|--------|------|
| OpenAI | 일정 생성·이미지 검색 (`OPENAI_API_KEY`) |
| AviationStack | 항공 검색 (기본 `enabled=false`, local에서 ON 가능) |
| Redis | 항공 추천 캐시 (`localhost:6379` 기본) |
| Kafka OUT | `planwith.ai-usage.reported` (usage-report ON 시) |
| Kafka IN | 없음 |
| Eureka | Discovery 등록 (local OFF) |
| Gateway | `:8000` 경유 예정 (스니펫은 deploy-check 경로 위주) |

---

## 5. 비기능 / 품질

- Hexagonal (`adapter` → `application` → `domain`)
- AI 사용량 Transactional Outbox + 스케줄 Relay (PESSIMISTIC_WRITE, retry/backoff)
- Service 계층 `@Transactional`
- 테스트: Domain / Application / Web / JPA·Redis·Kafka·Outbox 통합, EmbeddedKafka E2E 등 다수
- Testcontainers MySQL 사용 가능, test 프로필 H2

---

## 6. 배포 설정 요약

| 항목 | 기본값 | 비고 |
|------|--------|------|
| Compose 포트 | `8081` | local `18081` |
| Eureka | ON | local OFF |
| JPA DDL | `update` | local `validate` |
| AI Usage Report / Outbox Relay | OFF | `AI_USAGE_REPORT_ENABLED=true`로 ON |
| AviationStack | OFF | 키·enabled 필요 |
| Flight Redis Cache | ON | TTL 10m |
| Kafka bootstrap | `localhost:9092` | Docker에서는 env로 호스트명 지정 |

서버 env: `planwith-infra/env/schedule.env` (+ `common.env`)

---

## 7. 운영 주의사항

1. **Eureka 이름 불일치 가능**: yml `schedule-service` vs Compose `planwith-fo-schedule` → Gateway `lb://`와 반드시 맞출 것
2. Gateway 스니펫이 `/api/planwith-fo-schedule/**` 위주면 `/schedules`, `/flights` 등 실 API 라우팅을 수동 추가해야 함
3. AI 사용량 Outbox는 Relay OFF여도 **INSERT는 될 수 있음** → PENDING 적체 가능, Token 연동 시 Relay ON
4. 일반 CRUD/캘린더는 멤버 필터·소유권 검증이 AI revise보다 약함 (운영 보안 검토 필요)
5. Flight confirm은 영속하지 않음 (AI save와 혼동 주의)
6. AviationStack Free 플랜은 실시간/제한 항공만 가능 (문서화됨)
7. 시크릿은 env / gitignored `application-local.yml` 관리 (레포에 `.env.example` 없음)

---

## 8. 개발 완료 범위 (단계 요약)

```
01  Schedule Aggregate / Persistence / Soft Delete
02  Schedule CRUD + Calendar Query
03  AI Generate / Regenerate / Save
04  AI Revise (소유권 검증)
05  Flight Search / Recommend (Redis) / Confirm
06  Flight Location (정적 공항 맵)
07  AI Usage Outbox → Kafka Report
08  Deploy-check / Eureka / Compose 연동
09  Domain·Integration·Kafka E2E 테스트
```

---

## 9. 검증 상태

- [x] Domain / Unit Test
- [x] Integration Test (JPA / Redis / Outbox / Kafka)
- [x] AI Usage EmbeddedKafka E2E
- [ ] Gateway 전체 API 라우팅 정합 (스니펫 보강 필요)
- [ ] 운영 Kafka AI Usage Report 활성화 (인프라·Token 소비 준비 후)
- [ ] 일반 CRUD 소유권/멤버 스코프 강화 (보안 과제)

---

**RM 결론:** `planwith-fo-schedule`은 일정 CRUD·AI 생성/저장/수정·항공 검색/추천·AI 사용량 Outbox까지 기능 개발이 완료된 상태이며, 운영에서는 Eureka/Gateway 이름·경로 정합, Redis/OpenAI/AviationStack 키, (필요 시) AI Usage Kafka ON, CRUD 권한 정책을 맞추면 됩니다.
