package com.samrat.ai.developer.assessment.testcase.controller;

import com.samrat.ai.developer.assessment.exception.ProblemNotFoundException;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.testcase.dto.TestCaseDTOs;
import com.samrat.ai.developer.assessment.testcase.dto.TestCaseDTOs.AdminResponse;
import com.samrat.ai.developer.assessment.testcase.dto.TestCaseDTOs.PublicResponse;
import com.samrat.ai.developer.assessment.testcase.model.TestCase;
import com.samrat.ai.developer.assessment.testcase.repository.TestCaseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseRepository testCaseRepository;
    private final ProblemRepository problemRepository;

    // Public/User endpoint: Exposes sample test cases ONLY
    @GetMapping("/problems/{problemId}/test-cases")
    public ResponseEntity<List<PublicResponse>> getSampleTestCases(@PathVariable Long problemId) {
        List<TestCase> samples = testCaseRepository.findByProblemIdAndSampleTrueOrderByIdAsc(problemId);
        List<PublicResponse> response = samples.stream()
                .map(tc -> PublicResponse.builder()
                        .id(tc.getId())
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .sample(true)
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    // Admin endpoints
    @PostMapping("/admin/test-cases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponse> createTestCase(@Valid @RequestBody TestCaseDTOs.CreateRequest req) {
        Problem problem =problemRepository.findById(req.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found: " + req.getProblemId()));

        TestCase tc = TestCase.builder()
                .problem(problem)
                .input(req.getInput())
                .expectedOutput(req.getExpectedOutput())
                .hidden(req.isHidden())
                .sample(req.isSample())
                .points(req.getPoints())
                .build();

        tc = testCaseRepository.save(tc);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToAdminResponse(tc));
    }

    @GetMapping("/admin/problems/{problemId}/test-cases")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponse>> getAllTestCasesForAdmin(@PathVariable Long problemId) {
        List<TestCase> list = testCaseRepository.findByProblemIdOrderByIdAsc(problemId);
        return ResponseEntity.ok(list.stream().map(this::mapToAdminResponse).toList());
    }

    @DeleteMapping("/admin/test-cases/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id) {
        testCaseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private AdminResponse mapToAdminResponse(TestCase tc) {
        return AdminResponse.builder()
                .id(tc.getId())
                .problemId(tc.getProblem().getId())
                .input(tc.getInput())
                .expectedOutput(tc.getExpectedOutput())
                .hidden(tc.isHidden())
                .sample(tc.isSample())
                .points(tc.getPoints())
                .build();
    }
}
