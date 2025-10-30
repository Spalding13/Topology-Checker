package com.company.esd.result;

public class EsdRuleResult {
    private String ruleName;
    private boolean isViolated;
    private String description;
    private String message;

    public EsdRuleResult(String ruleName, boolean isViolated, String description, String message) {
        this.ruleName = ruleName;
        this.isViolated = isViolated;
        this.description = description;
        this.message = message;
    }

    public EsdRuleResult(String ruleName, boolean isViolated, String description) {
        this.ruleName = ruleName;
        this.isViolated = isViolated;
        this.description = description;
        this.message = "";
    }

    // Getters and setters
    public String getRuleName() {
        return ruleName;
    }

    public String getMessage() {
        return message;

    }
    public void setMessage(String msg){
        this.message = msg;
    }

    public String getDescription() {
        return description;
    }

    public boolean isViolated() {
        return isViolated;
    }


    //For debugging
    @Override
    public String toString() {
        return String.format(
                "Rule: %s | Violated: %s | Description: %s | Message: %s",
                ruleName,
                isViolated ? "Yes" : "No",
                description,
                message.isEmpty() ? "None" : message
        );
    }

}
