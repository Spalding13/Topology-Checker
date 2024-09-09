package com.company.esd.rule;

import com.company.esd.result.EsdRuleResult;
import com.company.esd.result.ResultCollector;

public abstract class AbstractParametricRule implements ParametricRule {

    // Common properties or methods for parametric rules

    public abstract void analyze(ResultCollector collector, EsdRuleResult result);

    protected boolean checkDeviceParameters(/* parameters */) {
        // Common logic to check device parameters
        return true; // Placeholder return
    }
}
