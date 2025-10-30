package com.company.esd.analyzer;

import com.company.esd.rule.EsdaRule;
import com.company.graph.Graph;
import com.company.esd.result.EsdRuleResult;
import com.company.esd.result.ResultCollector;
import com.company.esd.rule.StructuralRule;
import com.company.esd.rule.ParametricRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ESDAnalyzer {

    private List<StructuralRule> structuralRules;
    private List<ParametricRule> parametricRules;

    public ESDAnalyzer(List<StructuralRule> structuralRules, List<ParametricRule> parametricRules) {
        this.structuralRules = structuralRules;
        this.parametricRules = parametricRules;
    }

    // Optional constructor: only structural rules
    public ESDAnalyzer(List<StructuralRule> structuralRules) {
        this(structuralRules, new ArrayList<>());
    }

    public ResultCollector analyze(Graph graph, List<String> ports) throws IOException {
        ResultCollector resultCollector = new ResultCollector();

        // Iterate through all structural rules (if any)
        for (StructuralRule rule : structuralRules) {
            Graph pattern = rule.getPattern();

            NaiveSI naiveSI = new NaiveSI(graph, pattern);
            boolean patternMatched = naiveSI.isSubgraphIsomorphic();

            EsdRuleResult result = new EsdRuleResult(
                    rule.getClass().getSimpleName(),
                    !patternMatched, // violation if not matched
                    "Structural rule check: " + rule.getDescription()
            );

            System.out.println("Rule: " + rule.getClass().getSimpleName() + " → matched? " + patternMatched);

            resultCollector.addResult(result);
        }

        // Iterate through all parametric rules (if any)
        for (ParametricRule rule : parametricRules) {
            boolean valid = rule.analyze();

            EsdRuleResult result = new EsdRuleResult(
                    rule.getClass().getSimpleName(),
                    !valid, // violation if parametric condition not met
                    "Parametric rule check: " + rule.getDescription(),
                    rule.getMessage()
            );

            System.out.println("Rule: " + rule.getClass().getSimpleName() + " → valid? " + valid);

            resultCollector.addResult(result);
        }

        return resultCollector;
    }

}
