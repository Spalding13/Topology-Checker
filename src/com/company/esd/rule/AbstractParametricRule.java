package com.company.esd.rule;

import com.company.devicefactory.Device;
import com.company.esd.result.ResultCollector;

import java.util.List;

/**
 * Base class for parametric ESD rules.
 * Parametric rules validate numerical device parameters (e.g. area, nf, perimeter).
 * Unlike structural rules, they do not perform topology traversal.
 */
public abstract class AbstractParametricRule {

    /**
     * Analyzes the given list of devices according to the rule criteria.
     * @param devices list of all devices in the design
     * @param collector to record any violations
     */
    public abstract void analyze(List<Device> devices, ResultCollector collector);

    public abstract String getName();
    public abstract String getDescription();
}
