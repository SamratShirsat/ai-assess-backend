package com.samrat.ai.developer.assessment.admin.controller;

import com.samrat.ai.developer.assessment.admin.dto.AdminDTOs.*;
import com.samrat.ai.developer.assessment.admin.service.AdminUserService;
import com.samrat.ai.developer.assessment.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<UserAdminSummary>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(search, role, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDetailResponse> getUserDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserDetails(id));
    }
}