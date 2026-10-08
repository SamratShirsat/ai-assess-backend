package com.samrat.ai.developer.assessment.execution.service;

import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionResponse;
import com.samrat.ai.developer.assessment.execution.dto.ExecutionStatus;
import com.samrat.ai.developer.assessment.execution.dto.TestCaseResult;
import com.samrat.ai.developer.assessment.testcase.model.TestCase;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DockerSandboxService {

    private static final long TIMEOUT_SECONDS = 10;

    public CodeExecutionResponse executeCode(String language, String sourceCode, List<TestCase> testCases) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("code_exec_");
            String fileName = getFileName(language);
            Path codeFile = tempDir.resolve(fileName);
            Files.writeString(codeFile, sourceCode);

            List<TestCaseResult> results = new ArrayList<>();
            int passedCount = 0;
            long maxTime = 0;

            for (TestCase tc : testCases) {
                TestCaseResult tcResult = runSingleTestCase(language, tempDir, fileName, tc);
                results.add(tcResult);
                if (tcResult.isPassed()) {
                    passedCount++;
                }
                maxTime = Math.max(maxTime, tcResult.getExecutionTimeMs());
            }

            ExecutionStatus overallStatus = (passedCount == testCases.size()) ? ExecutionStatus.ACCEPTED : ExecutionStatus.WRONG_ANSWER;

            return CodeExecutionResponse.builder()
                    .status(overallStatus)
                    .totalTestCases(testCases.size())
                    .passedTestCases(passedCount)
                    .testResults(results)
                    .maxExecutionTimeMs(maxTime)
                    .build();

        } catch (Exception e) {
            return CodeExecutionResponse.builder()
                    .status(ExecutionStatus.RUNTIME_ERROR)
                    .compilerOutput(e.getMessage())
                    .passedTestCases(0)
                    .totalTestCases(testCases.size())
                    .build();
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir.toFile());
            }
        }
    }

    private TestCaseResult runSingleTestCase(String language, Path tempDir, String fileName, TestCase tc) {
        long startTime = System.currentTimeMillis();
        try {
            List<String> command = buildDockerCommand(language, tempDir.toString(), fileName);
            ProcessBuilder pb = new ProcessBuilder(command);

            Process process = pb.start();

            // Write test case input to container stdin
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(tc.getInput());
                writer.flush();
            }

            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!completed) {
                process.destroyForcibly();
                return TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .passed(false)
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .errorMessage("Time Limit Exceeded (5s)")
                        .executionTimeMs(executionTime)
                        .build();
            }

            String actualOutput = new String(process.getInputStream().readAllBytes()).trim();
            String errorOutput = new String(process.getErrorStream().readAllBytes()).trim();

            if (process.exitValue() != 0) {
                return TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .passed(false)
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .errorMessage(errorOutput.isEmpty() ? "Runtime Error" : errorOutput)
                        .executionTimeMs(executionTime)
                        .build();
            }

            boolean passed = actualOutput.equals(tc.getExpectedOutput().trim());

            return TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .passed(passed)
                    .input(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .actualOutput(actualOutput)
                    .executionTimeMs(executionTime)
                    .build();

        } catch (Exception e) {
            return TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .passed(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private List<String> buildDockerCommand(String language, String hostDir, String fileName) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-i");
        cmd.add("--net=none");
        cmd.add("--memory=256m");
        cmd.add("--cpus=1.0");
        cmd.add("-v");
        cmd.add(hostDir + ":/app");
        cmd.add("-w");
        cmd.add("/app");

        switch (language.toUpperCase()) {
            case "JAVA":
                cmd.add("openjdk:21-slim");
                cmd.add("bash");
                cmd.add("-c");
                cmd.add("javac " + fileName + " && java Main");
                break;
            case "PYTHON":
                cmd.add("python:3.11-slim");
                cmd.add("python3");
                cmd.add(fileName);
                break;
            case "JAVASCRIPT":
                cmd.add("node:20-slim");
                cmd.add("node");
                cmd.add(fileName);
                break;
            case "CPP":
                cmd.add("gcc:13");
                cmd.add("bash");
                cmd.add("-c");
                cmd.add("g++ -O2 " + fileName + " -o solution && ./solution");
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return cmd;
    }

    private String getFileName(String language) {
        return switch (language.toUpperCase()) {
            case "JAVA" -> "Main.java";
            case "PYTHON" -> "solution.py";
            case "JAVASCRIPT" -> "solution.js";
            case "CPP" -> "solution.cpp";
            default -> "solution.txt";
        };
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) deleteDirectory(f);
        }
        dir.delete();
    }
}
