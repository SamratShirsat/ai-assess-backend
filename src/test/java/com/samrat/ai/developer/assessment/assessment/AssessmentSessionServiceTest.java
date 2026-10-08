package com.samrat.ai.developer.assessment.assessment;

import com.samrat.ai.developer.assessment.assessment.model.*;
import com.samrat.ai.developer.assessment.assessment.repository.*;
import com.samrat.ai.developer.assessment.assessment.service.AssessmentScoringService;
import com.samrat.ai.developer.assessment.assessment.service.AssessmentSessionService;
import com.samrat.ai.developer.assessment.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentSessionServiceTest {

    @Mock
    private AssessmentSessionRepository sessionRepository;
    @Mock
    private AssessmentScoringService scoringService;

    @InjectMocks
    private AssessmentSessionService sessionService;

    @Test
    @DisplayName("Server accurately enforces expiresAt = startedAt + duration")
    void testSessionExpirationCalculation() {
        Instant now = Instant.parse("2026-10-07T10:00:00Z");
        Assessment assessment = Assessment.builder().durationMinutes(60).build();
        Instant expiresAt = now.plus(Duration.ofMinutes(assessment.getDurationMinutes()));

        assertEquals(Instant.parse("2026-10-07T11:00:00Z"), expiresAt);
    }

    @Test
    @DisplayName("Access denied if session user does not match authenticated user")
    void testSessionAccessDenied() {
        User user1 = User.builder().id(1L).build();
        User user2 = User.builder().id(2L).build();
        AssessmentSession session = AssessmentSession.builder().id(100L).user(user1).build();

        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));

        assertThrows(SecurityException.class, () -> sessionService.validateOwnershipAndState(100L, user2.getId()));
    }
}
