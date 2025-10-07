package com.SleepUp.SU.security.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    @GetMapping
    public ResponseEntity<HealthComponent> health() {
        HealthComponent healthComponent = healthEndpoint.health();
        int statusCode = healthComponent.getStatus().equals(org.springframework.boot.actuate.health.Status.UP) ? 200 : 503;
        return ResponseEntity.status(statusCode).body(healthComponent);
    }

}