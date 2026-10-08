package com.samrat.ai.developer.assessment.auth.controller;

import com.samrat.ai.developer.assessment.auth.dto.LoginRequest;
import com.samrat.ai.developer.assessment.auth.dto.LoginResponse;
import com.samrat.ai.developer.assessment.auth.dto.RegisterRequest;
import com.samrat.ai.developer.assessment.auth.dto.UserResponse;
import com.samrat.ai.developer.assessment.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}