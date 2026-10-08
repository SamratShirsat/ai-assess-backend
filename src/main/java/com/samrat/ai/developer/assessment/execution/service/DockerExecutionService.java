package com.samrat.ai.developer.assessment.execution.service;

import com.samrat.ai.developer.assessment.execution.config.CodeExecutionProperties;
import com.samrat.ai.developer.assessment.execution.dto.CodeExecutionResponse;
import com.samrat.ai.developer.assessment.execution.dto.ExecutionStatus;
import com.samrat.ai.developer.assessment.execution.model.LanguageExecutionConfig;
import com.samrat.ai.developer.assessment.execution.model.SupportedLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DockerExecutionService {

    private final CodeExecutionProperties properties;

    public CodeExecutionResponse execute(String language, String sourceCode) {
        return executeWithInput(SupportedLanguage.valueOf(language.toUpperCase()), sourceCode, "");
    }

    public CodeExecutionResponse executeWithInput(SupportedLanguage language, String sourceCode, String stdin) {
        if (sourceCode.getBytes(StandardCharsets.UTF_8).length > properties.getMaxSourceCodeBytes()) {
            return CodeExecutionResponse.builder()
                    .status(ExecutionStatus.EXECUTION_ERROR)
                    .message("Source code exceeds max size limit.")
                    .build();
        }

        LanguageExecutionConfig config = LanguageExecutionConfig.fromLanguage(language);
        Path workspaceDir = null;

        try {
            workspaceDir = Files.createTempDirectory("code-exec-");
            Path sourceFile = workspaceDir.resolve(config.getSourceFileName());
            Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);

            if (stdin != null && !stdin.isEmpty()) {
                Files.writeString(workspaceDir.resolve("input.txt"), stdin, StandardCharsets.UTF_8);
            }

            // Compilation Phase
            if (config.getCompileCommand() != null) {
                CodeExecutionResponse compileResult = runContainer(config, workspaceDir, config.getCompileCommand(), null, true);
                if (compileResult.getStatus() != ExecutionStatus.SUCCESS) {
                    return compileResult;
                }
            }

            // Execution Phase (Pipe input.txt into execution if stdin exists)
            String runCmd = config.getRunCommand();
            if (stdin != null && !stdin.isEmpty()) {
                runCmd = runCmd + " < input.txt";
            }

            return runContainer(config, workspaceDir, runCmd, stdin, false);

        } catch (IOException e) {
            log.error("Failed creating execution workspace", e);
            return CodeExecutionResponse.builder()
                    .status(ExecutionStatus.EXECUTION_ERROR)
                    .message("Failed workspace initialization.")
                    .build();
        } finally {
            if (workspaceDir != null) {
                deleteDirectoryQuietly(workspaceDir.toFile());
            }
        }
    }

    private CodeExecutionResponse runContainer(
            LanguageExecutionConfig config,
            Path workspaceDir,
            String scriptCommand,
            String stdin,
            boolean isCompilation
    ) {
        String containerName = "sandbox-" + UUID.randomUUID().toString().substring(0, 8);
        List<String> dockerCommand = new ArrayList<>(List.of(
                "docker", "run",
                "--name", containerName,
                "--rm",
                "-i",
                "--net=none",
                "--memory=" + properties.getMemoryLimit(),
                "--cpus=" + properties.getCpuLimit(),
                "--pids-limit=" + properties.getMaxProcesses(),
                "--read-only",
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=64m",
                "-v", workspaceDir.toAbsolutePath().toString() + ":/workspace:rw",
                "-w", "/workspace",
                config.getDockerImage(),
                "sh", "-c", scriptCommand
        ));

        long startTime = System.currentTimeMillis();
        Process process = null;

        try {
            ProcessBuilder pb = new ProcessBuilder(dockerCommand);
            process = pb.start();

            if (stdin != null) {
                try (OutputStream stream = process.getOutputStream()) {
                    stream.write(stdin.getBytes(StandardCharsets.UTF_8));
                }
            }

            boolean completed = process.waitFor(properties.getTimeoutMs(), TimeUnit.MILLISECONDS);
            long executionTimeMs = System.currentTimeMillis() - startTime;

            if (!completed) {
                killContainer(containerName);
                return CodeExecutionResponse.builder()
                        .status(ExecutionStatus.TIME_LIMIT_EXCEEDED)
                        .executionTimeMs(executionTimeMs)
                        .message("Time Limit Exceeded (" + (properties.getTimeoutMs() / 1000) + "s limit).")
                        .build();
            }

            String stdout = readLimitedStream(process.getInputStream(), properties.getMaxOutputBytes());
            String stderr = readLimitedStream(process.getErrorStream(), properties.getMaxOutputBytes());
            int exitCode = process.exitValue();

            if (stdout.endsWith("[OUTPUT_TRUNCATED]") || stderr.endsWith("[OUTPUT_TRUNCATED]")) {
                return CodeExecutionResponse.builder()
                        .status(ExecutionStatus.OUTPUT_LIMIT_EXCEEDED)
                        .stdout(stdout)
                        .stderr(stderr)
                        .executionTimeMs(executionTimeMs)
                        .exitCode(exitCode)
                        .message("Output limit exceeded (" + (properties.getMaxOutputBytes() / 1024) + " KB).")
                        .build();
            }

            if (exitCode != 0) {
                ExecutionStatus status = isCompilation ? ExecutionStatus.COMPILE_ERROR : ExecutionStatus.RUNTIME_ERROR;
                String msg = isCompilation ? "Compilation failed." : "Runtime execution error.";
                return CodeExecutionResponse.builder()
                        .status(status)
                        .stdout(stdout)
                        .stderr(stderr)
                        .executionTimeMs(executionTimeMs)
                        .exitCode(exitCode)
                        .message(msg)
                        .build();
            }

            return CodeExecutionResponse.builder()
                    .status(ExecutionStatus.SUCCESS)
                    .stdout(stdout)
                    .stderr(stderr)
                    .executionTimeMs(executionTimeMs)
                    .exitCode(0)
                    .message("Execution completed successfully.")
                    .build();

        } catch (Exception e) {
            log.error("Container execution exception for {}", containerName, e);
            killContainer(containerName);
            return CodeExecutionResponse.builder()
                    .status(ExecutionStatus.EXECUTION_ERROR)
                    .message("Execution failed due to internal sandbox error.")
                    .build();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private String readLimitedStream(InputStream is, long limitBytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        long totalRead = 0;
        boolean truncated = false;

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            if (totalRead + nRead > limitBytes) {
                int allowed = (int) (limitBytes - totalRead);
                if (allowed > 0) {
                    buffer.write(data, 0, allowed);
                }
                truncated = true;
                break;
            }
            buffer.write(data, 0, nRead);
            totalRead += nRead;
        }

        String result = buffer.toString(StandardCharsets.UTF_8);
        return truncated ? result + "\n[OUTPUT_TRUNCATED]" : result;
    }

    private void killContainer(String containerName) {
        try {
            new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    private void deleteDirectoryQuietly(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectoryQuietly(f);
            }
        }
        file.delete();
    }
}
