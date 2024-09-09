package com.company.esd.analyzer;

import com.company.graph.Graph;
import com.company.esd.result.ResultCollector;

import java.util.List;

public interface RuleAnalyzer {
    ResultCollector analyze(Graph graph, List<String> ports);
}
