package com.samrat.ai.developer.assessment.submission.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class OutputComparisonService {

    public boolean compare(String expected, String actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;

        String normalizedExpected = normalize(expected);
        String normalizedActual = normalize(actual);

        return normalizedExpected.equals(normalizedActual);
    }

    private String normalize(String text) {
        return Arrays.stream(text.replace("\r\n", "\n").split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"))
                .strip();
    }
}
