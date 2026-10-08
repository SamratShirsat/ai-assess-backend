package com.samrat.ai.developer.assessment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test/protected")
    public ResponseEntity<Map<String, String>> getProtectedResource(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "message", "You are authenticated",
                "user", authentication.getName()
        ));
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<Map<String, String>> getAdminDashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome to Admin Dashboard"));
    }
}
