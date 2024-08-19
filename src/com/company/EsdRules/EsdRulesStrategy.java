package com.company.EsdRules;

import com.company.graph.Graph;

interface EsdRuleStrategy {
    EsdRuleResult analyze(Graph graph);
}
