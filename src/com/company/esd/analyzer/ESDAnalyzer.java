package com.company.esd.analyzer;

import com.company.graph.Graph;
import com.company.esd.result.EsdRuleResult;
import com.company.esd.result.ResultCollector;
import com.company.esd.rule.StructuralRule;
import com.company.esd.rule.ParametricRule;

import java.util.List;

public class ESDAnalyzer {

    private List<StructuralRule> structuralRules;
    private List<ParametricRule> parametricRules;

    public ESDAnalyzer(List<StructuralRule> structuralRules, List<ParametricRule> parametricRules) {
        this.structuralRules = structuralRules;
        this.parametricRules = parametricRules;
    }

    public ResultCollector analyze(Graph graph, List<String> ports) {
        ResultCollector resultCollector = new ResultCollector();

        for (StructuralRule rule : structuralRules) {
            EsdRuleResult result = rule.analyze(graph, ports);
            resultCollector.addResult(result);

            // Perform parametric checks for devices in this result
            for (ParametricRule parametricRule : parametricRules) {
//                parametricRule.analyze(resultCollector, result);
            }
        }

        return resultCollector;
    }
}
