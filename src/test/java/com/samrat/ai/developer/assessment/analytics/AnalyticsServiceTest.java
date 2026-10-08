package com.samrat.ai.developer.assessment.analytics;

import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.adaptive.repository.UserSkillProfileRepository;
import com.samrat.ai.developer.assessment.analytics.dto.OverallAnalyticsResponse;
import com.samrat.ai.developer.assessment.analytics.model.PerformanceTrend;
import com.samrat.ai.developer.assessment.analytics.repository.AnalyticsRepository;
import com.samrat.ai.developer.assessment.analytics.service.AnalyticsService;
import com.samrat.ai.developer.assessment.submission.model.Submission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private UserSkillProfileRepository userSkillProfileRepository;
    @Mock
    private SkillPerformanceRepository skillPerformanceRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(analyticsRepository, userSkillProfileRepository, skillPerformanceRepository);
    }

    @Test
    void testEmptyAnalytics() {
        when(analyticsRepository.countTotalSubmissions(1L)).thenReturn(0L);

        OverallAnalyticsResponse response = analyticsService.getOverallAnalytics(1L);

        assertEquals(0, response.skillScore());
        assertEquals(0L, response.totalSubmissions());
        assertEquals(PerformanceTrend.STABLE, response.overallTrend());
    }

    @Test
    void testTrendImproving() {
        List<Submission> mockSubmissions = new ArrayList<>();
        // 5 recent high-scoring submissions (90 avg)
        for (int i = 0; i < 5; i++) {
            Submission s = new Submission();
            s.setScore(90);
            mockSubmissions.add(s);
        }
        // 5 previous low-scoring submissions (50 avg)
        for (int i = 0; i < 5; i++) {
            Submission s = new Submission();
            s.setScore(50);
            mockSubmissions.add(s);
        }

        when(analyticsRepository.findRecentSubmissions(eq(1L), any(Pageable.class))).thenReturn(mockSubmissions);

        PerformanceTrend trend = analyticsService.calculateUserTrend(1L, null);
        assertEquals(PerformanceTrend.IMPROVING, trend);
    }
}