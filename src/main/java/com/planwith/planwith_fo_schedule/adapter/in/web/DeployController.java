package com.planwith.planwith_fo_schedule.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_schedule.config.DeployProperties;

@RestController
@RequestMapping("/api/planwith-fo-schedule")
public class DeployController {

	private final DeployProperties deployProperties;

	public DeployController(DeployProperties deployProperties) {
		this.deployProperties = deployProperties;
	}

	@GetMapping("/deploy-check")
	public ResponseEntity<Map<String, String>> deployCheck() {
		return ResponseEntity.ok(Map.of(
				"service", "schedule-service",
				"marker", deployProperties.marker(),
				"message", "schedule-service deploy pipeline ok"
		));
	}
}
