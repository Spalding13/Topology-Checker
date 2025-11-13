package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.esd.analyzer.ESDAnalyzer;
import com.company.esd.rule.*;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;
import com.company.reducer.Reducer;
import com.company.reducer_benchmark.ReducerBenchmark;

import java.io.IOException;
import java.util.*;

public class Main {



    public static void main(String[] args) throws IOException {
        final boolean EXPERIMENT_MODE = true;


        // Step 1: Define ports
        List<String> ports = Arrays.asList("PAD", "VDD<1>", "GND<1>", "GND<2>");

        // Step 2: Read the .cdl input file into a string
        String inputPath = "E:\\ESD Checks\\input\\netlist_ultra_large.cdl";
        String input = NetlistReader.openFile(inputPath);

        // Step 3: Parse the netlist using StateMachine
        StateMachine stateMachine = new StateMachine();
        assert input != null;
        Map<String, List<String>> netlistInfo = stateMachine.parseNetlist(input);

        // Step 4: Create nets and devices from parsed information
        NetFactory netFactory = new NetFactory();
        List<Net> nets = netFactory.createNets(netlistInfo.get("nets"));

        List<Device> devices = DeviceFactory.createDevicesFromLines(netlistInfo.get("devices"));

        // Step 5: Build the graph using GraphFactory
        Graph graph = GraphFactory.buildGraph(devices, netFactory.getNetMap());

        // --- EXPERIMENT MODE ---
        if (EXPERIMENT_MODE) {
            System.out.println("\n=== Running Reducer Benchmark Experiment ===");
            ReducerBenchmark.run(graph);
            return;
        }

        // Step 6: Reduce the graph to simplify its structure
        graph = Reducer.reduce(graph);

        // Step 7: Create ESD rules
        List<StructuralRule> structuralRules = new ArrayList<>();
        List<ParametricRule> parametricRules = new ArrayList<>();

        EsdaRule structuralRule = new EsdaRule();
        EsdaParametricRule paramRule = new EsdaParametricRule();

        parametricRules.add(paramRule);
        structuralRules.add(structuralRule);

        System.out.println("Testing rule application...");
        // Uncomment and customize below when integrating rule analysis
        ESDAnalyzer analyzer = new ESDAnalyzer(structuralRules, parametricRules);
        analyzer.analyze(graph, ports);
        
    }


}
