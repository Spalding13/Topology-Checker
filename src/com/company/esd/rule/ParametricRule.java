package com.company.esd.rule;

import com.company.esd.result.EsdRuleResult;
import com.company.esd.result.ResultCollector;

public interface ParametricRule {
    void analyze(ResultCollector collector, EsdRuleResult result);
}
