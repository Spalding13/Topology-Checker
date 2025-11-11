package com.company.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RuleResultEntry {
    private final StringProperty ruleName;
    private final StringProperty description;
    private final StringProperty result;

    public RuleResultEntry(String ruleName, String description, String result) {
        this.ruleName = new SimpleStringProperty(ruleName);
        this.description = new SimpleStringProperty(description);
        this.result = new SimpleStringProperty(result);
    }

    public StringProperty ruleNameProperty() { return ruleName; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty resultProperty() { return result; }

    public String getRuleName() { return ruleName.get(); }
    public String getDescription() { return description.get(); }
    public String getResult() { return result.get(); }

    public void setResult(String result) { this.result.set(result); }
}
