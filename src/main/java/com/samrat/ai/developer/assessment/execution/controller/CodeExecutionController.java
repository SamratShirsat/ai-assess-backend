package com.samrat.ai.developer.assessment.execution.controller;

import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionRequest;
import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionResponse;
import com.samrat.ai.developer.assessment.execution.service.CodeExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    @PostMapping("/run")
    public ResponseEntity<CodeExecutionResponse> runCode(
            @Valid @RequestBody CodeExecutionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CodeExecutionResponse response = codeExecutionService.executeCode(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}