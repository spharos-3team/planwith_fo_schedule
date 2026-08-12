package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.config.DeployProperties;

@RestController
@RequestMapping("/api/planwith-fo-schedule")
public class DeployController {
	private static final Logger log = LoggerFactory.getLogger(DeployController.class);

	private final DeployProperties deployProperties;

	public DeployController(DeployProperties deployProperties) {
		this.deployProperties = deployProperties;
	}

	// 배포 상태 확인
	@GetMapping("/deploy-check")
	public ResponseEntity<Map<String, String>> deployCheck() {
		log.info("DeployController : GETdeployCheck : 배포 상태 확인 시작");
		log.trace("DeployController : GETdeployCheck : 설정에서 배포 마커 조회");
		Map<String, String> response = Map.of(
				"service", "schedule-service",
				"marker", deployProperties.marker(),
				"message", "schedule-service deploy pipeline ok"
		);
		log.debug("DeployController : GETdeployCheck : 배포 상태 확인 결과 - service={}", response.get("service"));
		log.info("DeployController : GETdeployCheck : 배포 상태 확인 완료");
		return ResponseEntity.ok(response);
	}
}
