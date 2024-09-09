package com.company.esd.result;

import java.util.ArrayList;
import java.util.List;

public class ResultCollector {
    private List<EsdRuleResult> results;

    public ResultCollector() {
        this.results = new ArrayList<>();
    }

    public void addResult(EsdRuleResult result) {
        results.add(result);
    }

    public List<EsdRuleResult> getResults() {
        return results;
    }
}
