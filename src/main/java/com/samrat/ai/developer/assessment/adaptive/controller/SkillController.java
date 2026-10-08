package com.samrat.ai.developer.assessment.adaptive.controller;

import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.SkillPerformanceResponse;
import com.samrat.ai.developer.assessment.adaptive.dto.AdaptiveDTOs.UserSkillProfileResponse;
import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.model.UserSkillProfile;
import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.adaptive.service.SkillCalculationService;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillCalculationService skillCalculationService;
    private final SkillPerformanceRepository performanceRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserSkillProfileResponse> getMySkillProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        UserSkillProfile profile = skillCalculationService.calculateUserSkillProfile(user.getId());
        List<SkillPerformance> performances = performanceRepository.findByUserSkillProfileId(profile.getId());

        List<SkillPerformanceResponse> skills = performances.stream().map(sp -> SkillPerformanceResponse.builder()
                .topic(sp.getTopic())
                .score(sp.getSkillScore() != null ? sp.getSkillScore().doubleValue() : null)
                .level(sp.getSkillLevel())
                .successRate(sp.getSuccessRate())
                .attempts(sp.getTotalAttempts())
                .acceptedAttempts(sp.getAcceptedAttempts())
                .currentDifficulty(sp.getCurrentDifficulty())
                .easySuccessRate(sp.getEasySuccessRate())
                .mediumSuccessRate(sp.getMediumSuccessRate())
                .hardSuccessRate(sp.getHardSuccessRate())
                .consistencyScore(sp.getConsistencyScore())
                .lastAttemptAt(sp.getLastAttemptAt())
                .build()).toList();

        return ResponseEntity.ok(UserSkillProfileResponse.builder()
                .overallScore(profile.getOverallScore())
                .overallLevel(profile.getOverallLevel())
                .lastCalculatedAt(profile.getLastCalculatedAt())
                .skills(skills)
                .build());
    }

    @PostMapping("/admin/recalculate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> recalculateUserSkill(@PathVariable Long userId) {
        skillCalculationService.calculateUserSkillProfile(userId);
        return ResponseEntity.ok().build();
    }
}
