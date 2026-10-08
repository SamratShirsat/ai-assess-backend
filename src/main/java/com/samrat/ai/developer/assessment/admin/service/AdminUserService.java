// AdminUserService.java
package com.samrat.ai.developer.assessment.admin.service;

import com.samrat.ai.developer.assessment.adaptive.model.SkillPerformance;
import com.samrat.ai.developer.assessment.adaptive.repository.SkillPerformanceRepository;
import com.samrat.ai.developer.assessment.admin.dto.AdminDTOs.*;
import com.samrat.ai.developer.assessment.submission.model.SubmissionStatus;
import com.samrat.ai.developer.assessment.submission.repository.SubmissionRepository;
import com.samrat.ai.developer.assessment.user.model.Role;
import com.samrat.ai.developer.assessment.user.model.User;
import com.samrat.ai.developer.assessment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final SkillPerformanceRepository skillPerformanceRepository;

    @Transactional(readOnly = true)
    public Page<UserAdminSummary> getUsers(String search, Role role, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
        } else if (role != null) {
            users = userRepository.findAllByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(u -> new UserAdminSummary(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole(),
                u.getCreatedAt(),
                submissionRepository.countByUserId(u.getId()),
                0L // Assessment count
        ));
    }

    @Transactional(readOnly = true)
    public UserAdminDetailResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        long totalSubs = submissionRepository.countByUserId(userId);
        long acceptedSubs = submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.ACCEPTED);
        Double avg = submissionRepository.findAverageScoreByUserId(userId);
        BigDecimal avgScore = avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        List<SkillPerformance> perfs = skillPerformanceRepository.findAllByUserSkillProfileUserId(userId);
        List<UserAdminDetailResponse.UserTopicSkillOverview> skills = perfs.stream()
                .map(sp -> new UserAdminDetailResponse.UserTopicSkillOverview(
                        sp.getTopic(),
                        BigDecimal.valueOf(sp.getSkillScore() != null ? sp.getSkillScore() : 0),
                        sp.getSkillLevel() != null ? sp.getSkillLevel().name() : "NOVICE"
                )).toList();

        return new UserAdminDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                totalSubs,
                acceptedSubs,
                avgScore,
                0L,
                skills
        );
    }
}
