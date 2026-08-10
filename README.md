# schedule-service

PlanWith 일정 Aggregate를 소유하는 Spring Boot 서비스입니다. 내부 구조는 `adapter -> application -> domain` 의존 방향을 따르는 Hexagonal Architecture입니다.

| 항목 | 값 |
| --- | --- |
| Spring/Eureka 이름 | `schedule-service` |
| Compose 이름 | `planwith-fo-schedule` |
| 이미지 | `planwith/planwith-fo-schedule:latest` |
| 로컬 기본 포트 | 랜덤 포트 (`0`) |
| Compose 포트 | `8081` (`SERVER_PORT` 주입) |
| 배포 확인 | `GET /api/planwith-fo-schedule/deploy-check` |

## 로컬 실행

MySQL 접속 정보를 환경변수로 주입한 뒤 실행합니다.

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/schedule'
$env:DB_USERNAME='schedule'
$env:DB_PASSWORD='<secret>'
.\gradlew.bat bootRun
```

- `SERVER_PORT`를 지정하지 않으면 로컬 포트가 임의 할당됩니다.
- Swagger UI: `/swagger-ui/index.html`
- 일정 생성: `POST /api/v1/schedules`
- Deploy check: `GET /api/planwith-fo-schedule/deploy-check`

## 구조

- `domain`: Spring/JPA에 독립적인 `Schedule`, `ScheduleItem` Aggregate
- `application`: UseCase, Repository Port, 트랜잭션 경계
- `adapter.in.web`: REST 요청/응답 및 검증
- `adapter.out.persistence`: JPA Entity와 Repository Adapter
- `config`: 서비스 설정

Kafka, Redis, CQRS, Outbox는 현재 구성에 포함하지 않습니다.

## 서버 배포 확인

1. GitHub 레포 생성 후 push (`develop` 또는 `main`)
2. `planwith-infra` compose에 `planwith-fo-schedule` 등록 후 서버 `C:\planwith\docker-compose.yml` 반영
3. Actions Deploy 성공 확인
4. `http://<서버IP>:8081/api/planwith-fo-schedule/deploy-check` 응답의 `marker` 확인
