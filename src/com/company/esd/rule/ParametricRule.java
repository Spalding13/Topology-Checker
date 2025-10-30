package com.company.esd.rule;

import com.company.esd.result.EsdRuleResult;
import com.company.esd.result.ResultCollector;

public interface ParametricRule {

    boolean analyze();
    String getDescription();

    String getName();
    String getMessage();
}
