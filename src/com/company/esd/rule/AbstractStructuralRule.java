package com.company.esd.rule;
import com.company.NetlistReader;
import com.company.StateMachine;
import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Graph;
import com.company.esd.result.EsdRuleResult;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractStructuralRule implements StructuralRule {

    // Common properties or methods for structural rules

    public abstract EsdRuleResult analyze(Graph graph, List<String> ports);

    protected void traverseGraph(Graph graph) {
        // Common traversal logic for graph nodes
    }

    protected Graph initializePattern() throws IOException {

        // netlistInfo => "devices", "nets", "designDetails"
        StateMachine stateMachine = new StateMachine();

        String input = NetlistReader.openFile("E:\\ESD Checks\\input\\esda_ptrn.cdl");

        Map<String, List<String>> netlistInfo = stateMachine.parseNetlist(input);

        List<Device> devices = DeviceFactory.createDevicesFromLines(netlistInfo.get("devices"));

        Graph graph = GraphFactory.buildGraph(devices);

        return graph;
    }
}
