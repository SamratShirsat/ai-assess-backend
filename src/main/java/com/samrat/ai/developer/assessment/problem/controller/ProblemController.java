package com.samrat.ai.developer.assessment.problem.controller;

import com.samrat.ai.developer.assessment.problem.dto.ProblemCreateRequest;
import com.samrat.ai.developer.assessment.problem.dto.ProblemResponse;
import com.samrat.ai.developer.assessment.problem.dto.ProblemUpdateRequest;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping("/api/problems")
    public ResponseEntity<Page<ProblemResponse>> getAllProblems(
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Topic topic,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int validatedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by("id").ascending());
        return ResponseEntity.ok(problemService.getAllProblems(difficulty, topic, search, pageable));
    }

    @GetMapping("/api/problems/{id}")
    public ResponseEntity<ProblemResponse> getProblemById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    @PostMapping("/api/admin/problems")
    public ResponseEntity<ProblemResponse> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createProblem(request));
    }

    @PutMapping("/api/admin/problems/{id}")
    public ResponseEntity<ProblemResponse> updateProblem(
            @PathVariable Long id,
            @Valid @RequestBody ProblemUpdateRequest request
    ) {
        return ResponseEntity.ok(problemService.updateProblem(id, request));
    }

    @DeleteMapping("/api/admin/problems/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }
}
