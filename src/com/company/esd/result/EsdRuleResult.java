package com.company.esd.result;

public class EsdRuleResult {
    private String ruleName;
    private boolean isViolated;
    private String description;

    public EsdRuleResult(String ruleName, boolean isViolated, String description) {
        this.ruleName = ruleName;
        this.isViolated = isViolated;
        this.description = description;
    }

    // Getters and setters
}
