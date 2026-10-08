package com.samrat.ai.developer.assessment.ai.service;

import com.samrat.ai.developer.assessment.ai.client.AiClient;
import com.samrat.ai.developer.assessment.ai.config.AiProperties;
import com.samrat.ai.developer.assessment.ai.dto.HintRequest;
import com.samrat.ai.developer.assessment.ai.dto.HintResponse;
import com.samrat.ai.developer.assessment.ai.model.AiHint;
import com.samrat.ai.developer.assessment.ai.exception.AiRateLimitException;
import com.samrat.ai.developer.assessment.ai.model.HintLevel;
import com.samrat.ai.developer.assessment.ai.repository.AiHintRepository;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiHintService {

    private final AiClient aiClient;
    private final AiPromptService promptService;
    private final AiResponseParser responseParser;
    private final AiHintRepository aiHintRepository;
    private final ProblemRepository problemRepository;
    private final AiProperties aiProperties;

    @Transactional
    public HintResponse getHint(Long userId, HintRequest request) {
        checkRateLimit(userId);

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        // Enforce Hint Progression Rule
        enforceHintProgression(userId, problem.getId(), request.getHintLevel());

        // Check if existing hint level already generated
        List<AiHint> existing = aiHintRepository.findByUserIdAndProblemIdOrderByHintLevelAsc(userId, problem.getId());
        for (AiHint h : existing) {
            if (h.getHintLevel() == request.getHintLevel()) {
                return HintResponse.builder()
                        .problemId(problem.getId())
                        .hintLevel(h.getHintLevel())
                        .hint(h.getHintText())
                        .build();
            }
        }

        String systemPrompt = promptService.buildHintSystemPrompt(request.getHintLevel());
        String userPrompt = promptService.buildHintUserPrompt(
                problem.getTitle(),
                problem.getDescription(),
                problem.getConstraints(),
                request.getLanguage(),
                request.getSourceCode()
        );

        String rawResponse = aiClient.generate(systemPrompt, userPrompt);
        String hintText = responseParser.parseHint(rawResponse);

        AiHint aiHint = AiHint.builder()
                .userId(userId)
                .problemId(problem.getId())
                .hintLevel(request.getHintLevel())
                .hintText(hintText)
                .build();
        aiHintRepository.save(aiHint);

        return HintResponse.builder()
                .problemId(problem.getId())
                .hintLevel(request.getHintLevel())
                .hint(hintText)
                .build();
    }

    private void enforceHintProgression(Long userId, Long problemId, HintLevel requested) {
        if (requested == HintLevel.HINT_2) {
            boolean hasHint1 = aiHintRepository.existsByUserIdAndProblemIdAndHintLevel(userId, problemId, HintLevel.HINT_1);
            if (!hasHint1) {
                throw new IllegalArgumentException("You must request Hint 1 before Hint 2.");
            }
        } else if (requested == HintLevel.HINT_3) {
            boolean hasHint2 = aiHintRepository.existsByUserIdAndProblemIdAndHintLevel(userId, problemId, HintLevel.HINT_2);
            if (!hasHint2) {
                throw new IllegalArgumentException("You must request Hint 2 before Hint 3.");
            }
        }
    }

    private void checkRateLimit(Long userId) {
        OffsetDateTime since = OffsetDateTime.now().minusMinutes(aiProperties.getRateLimitWindowMinutes());
        long count = aiHintRepository.countByUserIdAndCreatedAtAfter(userId, since);
        if (count >= aiProperties.getHintRateLimit()) {
            throw new AiRateLimitException("AI Hint rate limit exceeded. Please wait before requesting more hints.");
        }
    }
}
