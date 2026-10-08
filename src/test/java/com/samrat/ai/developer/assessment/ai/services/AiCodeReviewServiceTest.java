package com.samrat.ai.developer.assessment.ai.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samrat.ai.developer.assessment.ai.client.AiClient;
import com.samrat.ai.developer.assessment.ai.config.AiProperties;
import com.samrat.ai.developer.assessment.ai.dto.CodeReviewRequest;
import com.samrat.ai.developer.assessment.ai.dto.CodeReviewResponse;
import com.samrat.ai.developer.assessment.ai.repository.AiReviewRepository;
import com.samrat.ai.developer.assessment.ai.service.AiCodeReviewService;
import com.samrat.ai.developer.assessment.ai.service.AiPromptService;
import com.samrat.ai.developer.assessment.ai.service.AiResponseParser;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiCodeReviewServiceTest {

    @Mock private AiClient aiClient;
    @Mock private AiPromptService promptService;
    @Mock private AiResponseParser responseParser;
    @Mock private AiReviewRepository aiReviewRepository;
    @Mock private SubmissionRepository submissionRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private AiProperties aiProperties;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AiCodeReviewService aiCodeReviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(aiProperties.getRateLimitWindowMinutes()).thenReturn(60);
        when(aiProperties.getReviewRateLimit()).thenReturn(5);
    }

    @Test
    void testReviewCodeSuccess() {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setTitle("Two Sum");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(promptService.buildReviewSystemPrompt()).thenReturn("System");
        when(promptService.buildReviewUserPrompt(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn("User");
        when(aiClient.generate("System", "User")).thenReturn("{}");

        CodeReviewResponse mockResponse = CodeReviewResponse.builder().summary("Great work").build();
        when(responseParser.parseCodeReview("{}")).thenReturn(mockResponse);

        CodeReviewRequest req = new CodeReviewRequest();
        req.setProblemId(1L);
        req.setSourceCode("public class Solution {}");
        req.setLanguage("JAVA");

        CodeReviewResponse result = aiCodeReviewService.reviewCode(100L, req);

        assertNotNull(result);
        assertEquals("Great work", result.getSummary());
        verify(aiReviewRepository, times(1)).save(any());
    }
}
