package com.company.esd.rule;

import com.company.graph.Graph;
import com.company.esd.result.EsdRuleResult;

import java.util.List;

public interface StructuralRule {
    EsdRuleResult analyze(Graph graph, List<String> ports);
}
