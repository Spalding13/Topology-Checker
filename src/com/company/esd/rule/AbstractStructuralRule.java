package com.company.esd.rule;
import com.company.NetlistReader;
import com.company.NetlistInterpreter;
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
        // Step 1: Parse the input netlist file
        String inputPath = "E:\\ESD Checks\\input\\esda_ptrn.cdl";
        NetlistInterpreter netlistInterpreter = new NetlistInterpreter();
        String input = NetlistReader.openFile(inputPath);

        // Step 2: Extract netlist information
        Map<String, List<String>> netlistInfo = netlistInterpreter.parseNetlist(input);

        // Step 3: Create nets and generate a map of nets
        NetFactory netFactory = new NetFactory();
        List<Net> nets = netFactory.createNets(netlistInfo.get("nets"));
        Map<String, Net> netMap = netFactory.getNetMap(); // Efficient net lookup

        // Step 4: Create devices from netlist information
        List<Device> devices = DeviceFactory.createDevicesFromLines(netlistInfo.get("devices"));

        // Step 5: Build the graph using devices and the net map
        Graph graph = GraphFactory.buildGraph(devices, netMap);

        // Step 6: Return the constructed graph
        return graph;
    }
}
