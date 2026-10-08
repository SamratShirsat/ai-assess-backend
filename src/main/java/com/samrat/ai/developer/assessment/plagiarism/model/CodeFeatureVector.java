package com.samrat.ai.developer.assessment.plagiarism.model;

import java.util.Map;

public class CodeFeatureVector {
    private int tokenCount;
    private int loopCount;
    private int conditionalCount;
    private int functionCount;
    private int returnCount;
    private int dataStructureUsageCount;
    private Map<String, Integer> keywordFrequencies;

    public CodeFeatureVector() {}

    public CodeFeatureVector(int tokenCount, int loopCount, int conditionalCount, int functionCount, int returnCount, int dataStructureUsageCount, Map<String, Integer> keywordFrequencies) {
        this.tokenCount = tokenCount;
        this.loopCount = loopCount;
        this.conditionalCount = conditionalCount;
        this.functionCount = functionCount;
        this.returnCount = returnCount;
        this.dataStructureUsageCount = dataStructureUsageCount;
        this.keywordFrequencies = keywordFrequencies;
    }

    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
    public int getLoopCount() { return loopCount; }
    public void setLoopCount(int loopCount) { this.loopCount = loopCount; }
    public int getConditionalCount() { return conditionalCount; }
    public void setConditionalCount(int conditionalCount) { this.conditionalCount = conditionalCount; }
    public int getFunctionCount() { return functionCount; }
    public void setFunctionCount(int functionCount) { this.functionCount = functionCount; }
    public int getReturnCount() { return returnCount; }
    public void setReturnCount(int returnCount) { this.returnCount = returnCount; }
    public int getDataStructureUsageCount() { return dataStructureUsageCount; }
    public void setDataStructureUsageCount(int dataStructureUsageCount) { this.dataStructureUsageCount = dataStructureUsageCount; }
    public Map<String, Integer> getKeywordFrequencies() { return keywordFrequencies; }
    public void setKeywordFrequencies(Map<String, Integer> keywordFrequencies) { this.keywordFrequencies = keywordFrequencies; }
}