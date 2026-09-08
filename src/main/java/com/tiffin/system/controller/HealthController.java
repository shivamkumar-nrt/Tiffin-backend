package com.tiffin.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final Instant startTime = Instant.now();

    @Value("${spring.application.name:tiffin-service-backend}")
    private String appName;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", appName);
        response.put("serverTime", Instant.now().toString());
        response.put("uptimeSince", startTime.toString());
        return ResponseEntity.ok(response);
    }
}