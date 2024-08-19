package com.company.EsdRules;

import com.company.graph.Graph;

/**
 All I/O ports must be connected to the following combination:
 1. One down diode satisfying rule ESDA1 + One or more HBM up diodes satisfying rule ESDA2
 2. One down diode satisfying rule ESDA1 + ESDNFET satisfying rule ESDA3
 **/
public class EsdaRuleStrategy implements EsdRuleStrategy {
    @Override
    public EsdRuleResult analyze(Graph graph) {
        // Implement logic for ESDA rule
        boolean isViolated = false;// determine if the rule is violated
        String description = "/** " +
                " All I/O ports must be connected to the following combination:\n" +
                " 1. One down diode satisfying rule ESDA1 + One or more HBM up diodes satisfying rule ESDA2\n" +
                " 2. One down diode satisfying rule ESDA1 + ESDNFET satisfying rule ESDA3\n" +
                " **/";// provide details about the violation





        return new EsdRuleResult("ESDA Rule", isViolated, description);
    }
}

/**
 * A down diode must have a perimeter >= 450um and nf >= 3
 **/

class Esda1RuleStrategy implements EsdRuleStrategy {
    @Override
    public EsdRuleResult analyze(Graph graph) {
        // Implement logic for ESDA rule
        boolean isViolated = false;// determine if the rule is violated
        String description = "";// provide details about the violation


        return new EsdRuleResult("ESDA Rule", isViolated, description);
    }
}
