package com.samrat.ai.developer.assessment.config;

import com.samrat.ai.developer.assessment.problem.model.Difficulty;
import com.samrat.ai.developer.assessment.problem.model.Problem;
import com.samrat.ai.developer.assessment.problem.model.Topic;
import com.samrat.ai.developer.assessment.problem.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ProblemDataInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;

    @Override
    public void run(String... args) {
        if (problemRepository.count() > 0) {
            return; // Avoid duplicating problems if already seeded
        }

        List<Problem> initialProblems = List.of(
                Problem.builder()
                        .title("Two Sum")
                        .description("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.")
                        .difficulty(Difficulty.EASY)
                        .topic(Topic.ARRAY)
                        .constraints("2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9")
                        .inputFormat("nums array, target integer")
                        .outputFormat("[index1, index2]")
                        .starterCode("class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        return new int[]{};\n    }\n}")
                        .expectedTimeComplexity("O(n)")
                        .expectedSpaceComplexity("O(n)")
                        .build(),

                Problem.builder()
                        .title("Valid Parentheses")
                        .description("Given a string s containing just '(', ')', '{', '}', '[' and ']', determine if the input string is valid.")
                        .difficulty(Difficulty.EASY)
                        .topic(Topic.STACK)
                        .constraints("1 <= s.length <= 10^4")
                        .inputFormat("String s")
                        .outputFormat("boolean")
                        .starterCode("class Solution {\n    public boolean isValid(String s) {\n        return false;\n    }\n}")
                        .expectedTimeComplexity("O(n)")
                        .expectedSpaceComplexity("O(n)")
                        .build(),

                Problem.builder()
                        .title("Coin Change")
                        .description("You are given an integer array coins and an integer amount. Return the fewest number of coins needed to make up that amount.")
                        .difficulty(Difficulty.MEDIUM)
                        .topic(Topic.DYNAMIC_PROGRAMMING)
                        .constraints("1 <= coins.length <= 12\n0 <= amount <= 10^4")
                        .inputFormat("coins array, amount integer")
                        .outputFormat("Minimum coins count or -1")
                        .starterCode("class Solution {\n    public int coinChange(int[] coins, int amount) {\n        return -1;\n    }\n}")
                        .expectedTimeComplexity("O(amount * coins.length)")
                        .expectedSpaceComplexity("O(amount)")
                        .build()
        );

        problemRepository.saveAll(initialProblems);
    }
}