package com.company.EsdRules;

public class EsdRuleResult {
    private String ruleName;
    private boolean isViolated;
    private String description; // Details about the violation or success
    // Add more fields if needed

    public EsdRuleResult(String ruleName, boolean isViolated, String description) {
        this.ruleName = ruleName;
        this.isViolated = isViolated;
        this.description = description;
    }

    // Getters and setters
}