package com.company.si_benchmark;

import com.company.NetlistInterpreter;
import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.esd.analyzer.AdvancedSI;
import com.company.esd.analyzer.NaiveSI;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.io.IOException;
import java.util.*;

public class SIBenchmark {

    // ============================================================
    // BENCHMARK CONFIGURATION
    // ============================================================
    private static final int COPIES = 229;     // how many noise blocks to generate
    private static final int WARMUP = 20;      // warmup calls to let JIT optimize
    private static final int RUNS   = 15;      // timed test repetitions

    // Toggle node order shuffle (true makes SI harder)
    private static final boolean SHUFFLE_GRAPH_NODES = true;

    // ============================================================
    // PUBLIC ENTRY
    // ============================================================
    public static void runSynthetic() throws IOException {

        System.out.println("=== SI Benchmark (Synthetic Netlist) ===");

        // --------------------------------------------------------
        // 1) Generate deterministic synthetic netlist
        // --------------------------------------------------------
        String netlistText = NetlistGeneratorSI.generatePositiveNetlist(COPIES);

        // --------------------------------------------------------
        // 2) Parse CDL
        // --------------------------------------------------------
        NetlistInterpreter sm = new NetlistInterpreter();
        Map<String, List<String>> parsed = sm.parseNetlist(netlistText);

        // Build nets
        NetFactory nf = new NetFactory();
        List<Net> nets = nf.createNets(parsed.get("nets"));
        Map<String, Net> netMap = nf.getNetMap();

        // Build devices
        List<Device> devices = DeviceFactory.createDevicesFromLines(parsed.get("devices"));

        // --------------------------------------------------------
        // 3) Build graph
        // --------------------------------------------------------
        Graph circuitGraph = GraphFactory.buildGraph(devices, netMap);

        if (SHUFFLE_GRAPH_NODES) {
            circuitGraph.shuffleNodeOrder(); // keeps determinism unless seed changes
        }

        // --------------------------------------------------------
        // 4) Build pattern graph from EsdaRule
        // --------------------------------------------------------
        var rule = new com.company.esd.rule.EsdaRule();
        Graph patternGraph = rule.getPattern();

        // --------------------------------------------------------
        // 5) Print sizes
        // --------------------------------------------------------
        System.out.println();
        System.out.println("Circuit nodes: " + circuitGraph.getNodes().size());
        System.out.println("Pattern nodes: " + patternGraph.getNodes().size());
        System.out.println("---------------------");

        // --------------------------------------------------------
        // 6) Run benchmarks
        // --------------------------------------------------------
        BenchmarkResult naive = benchmarkEngine("NaiveSI", circuitGraph, patternGraph, true);
        BenchmarkResult fast  = benchmarkEngine("FastSI",  circuitGraph, patternGraph, false);

        // --------------------------------------------------------
        // 7) Print results
        // --------------------------------------------------------
        System.out.printf("NaiveSI: match=%s, avg=%.3f ms%n", naive.match, naive.avgMs);
        System.out.printf("FastSI : match=%s, avg=%.3f ms%n", fast.match,  fast.avgMs);

        if (naive.match != fast.match)
            System.err.println("⚠ ERROR: NaiveSI and FastSI disagree on result!");
        else
            System.out.println("✓ Results consistent (" + naive.match + ")");

        System.out.printf("Speedup: %.2fx%n", (naive.avgMs / fast.avgMs));
        System.out.println("---------------------");
    }

    // ============================================================
    // RESULT STRUCTURE
    // ============================================================
    public record BenchmarkResult(boolean match, double avgMs) {}

    // ============================================================
    // CORE BENCHMARK METHOD
    // ============================================================
    private static BenchmarkResult benchmarkEngine(
            String label,
            Graph circuitGraph,
            Graph patternGraph,
            boolean useNaive)
    {
        long totalNs = 0;
        boolean last = false;

        // --- warmup ---
        for (int i = 0; i < WARMUP; i++) {
            if (useNaive) new NaiveSI(circuitGraph, patternGraph).isSubgraphIsomorphic();
            else          new AdvancedSI(circuitGraph, patternGraph).isSubgraphIsomorphic();
        }

        // --- timed runs ---
        for (int i = 0; i < RUNS; i++) {
            long t1 = System.nanoTime();

            last = (useNaive ?
                    new NaiveSI(circuitGraph, patternGraph).isSubgraphIsomorphic() :
                    new AdvancedSI(circuitGraph, patternGraph).isSubgraphIsomorphic());

            long t2 = System.nanoTime();
            totalNs += (t2 - t1);
        }

        return new BenchmarkResult(last, (totalNs / (double) RUNS) / 1_000_000.0);
    }
}
