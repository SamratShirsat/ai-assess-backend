package com.samrat.ai.developer.assessment.problem.service;

import com.samrat.ai.developer.assessment.exception.ProblemNotFoundException;
import com.samrat.ai.developer.assessment.problem.dto.ProblemCreateRequest;
import com.samrat.ai.developer.assessment.problem.dto.ProblemResponse;
import com.samrat.ai.developer.assessment.problem.dto.ProblemUpdateRequest;
import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;

    public Page<ProblemResponse> getAllProblems(Difficulty difficulty, Topic topic, String search, Pageable pageable) {
        // Sanitize search: empty or whitespace-only strings must be treated as null
        String sanitizedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        return problemRepository.findWithFilters(difficulty, topic, sanitizedSearch, pageable)
                .map(this::mapToResponse);
    }

    public ProblemResponse getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found with id: " + id));
        return mapToResponse(problem);
    }

    @Transactional
    public ProblemResponse createProblem(ProblemCreateRequest request) {
        if (problemRepository.existsByTitle(request.getTitle().trim())) {
            throw new IllegalArgumentException("Problem with title '" + request.getTitle() + "' already exists");
        }

        Problem problem = Problem.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .topic(request.getTopic())
                .constraints(request.getConstraints())
                .inputFormat(request.getInputFormat())
                .outputFormat(request.getOutputFormat())
                .starterCode(request.getStarterCode())
                .expectedTimeComplexity(request.getExpectedTimeComplexity())
                .expectedSpaceComplexity(request.getExpectedSpaceComplexity())
                .build();

        Problem savedProblem = problemRepository.save(problem);
        return mapToResponse(savedProblem);
    }

    @Transactional
    public ProblemResponse updateProblem(Long id, ProblemUpdateRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found with id: " + id));

        // Verify title uniqueness if the title is being changed
        String updatedTitle = request.getTitle().trim();
        if (!problem.getTitle().equalsIgnoreCase(updatedTitle) && problemRepository.existsByTitle(updatedTitle)) {
            throw new IllegalArgumentException("Problem with title '" + updatedTitle + "' already exists");
        }

        problem.setTitle(updatedTitle);
        problem.setDescription(request.getDescription());
        problem.setDifficulty(request.getDifficulty());
        problem.setTopic(request.getTopic());
        problem.setConstraints(request.getConstraints());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setStarterCode(request.getStarterCode());
        problem.setExpectedTimeComplexity(request.getExpectedTimeComplexity());
        problem.setExpectedSpaceComplexity(request.getExpectedSpaceComplexity());

        Problem updatedProblem = problemRepository.save(problem);
        return mapToResponse(updatedProblem);
    }

    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new ProblemNotFoundException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    private ProblemResponse mapToResponse(Problem problem) {
        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .topic(problem.getTopic())
                .constraints(problem.getConstraints())
                .inputFormat(problem.getInputFormat())
                .outputFormat(problem.getOutputFormat())
                .starterCode(problem.getStarterCode())
                .expectedTimeComplexity(problem.getExpectedTimeComplexity())
                .expectedSpaceComplexity(problem.getExpectedSpaceComplexity())
                .createdAt(problem.getCreatedAt())
                .updatedAt(problem.getUpdatedAt())
                .build();
    }
}