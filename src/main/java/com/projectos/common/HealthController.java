package com.projectos.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final String appVersion;

    public HealthController(@Value("${projectos.version}") String appVersion) {
        this.appVersion = appVersion;
    }

    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse("UP", appVersion);
    }

    public record HealthResponse(String status, String version) {
    }
}
