package com.company.reducer_benchmark;

import com.company.graph.Graph;
import com.company.netFactory.Net;
import com.company.reducer.ParallelReducer;

import java.util.Map;

/**
 * Benchmark comparing sequential vs parallel reducers.
 */
public class ReducerBenchmark {

    public static void run(Graph original) {

        System.out.println("\n========== Reducer Performance Benchmark ==========");

        Map<String, Net> netMap = original.getNetMap();

        // ------------------ Sequential ------------------
        long t1 = System.nanoTime();
        Graph seqGraph = SequentialReducer.reduce(original.deepCopy(netMap));
        long t2 = System.nanoTime();
        long seqTime = t2 - t1;

        // ------------------ Parallel ------------------
        long t3 = System.nanoTime();
        Graph parGraph = ParallelReducer.reduce(original.deepCopy(netMap));
        long t4 = System.nanoTime();
        long parTime = t4 - t3;

        // Count devices
        int seqCount = seqGraph.deviceNodeMap.size();
        int parCount = parGraph.deviceNodeMap.size();

        // ------------------ Results ------------------
        System.out.println("\n================ Benchmark Results ================");
        System.out.printf("Sequential Devices: %d%n", seqCount);
        System.out.printf("Parallel Devices:   %d%n", parCount);

        // Convert to milliseconds for more visible output and format with 3 decimals
        double seqMs = seqTime / 1_000_000.0;
        double parMs = parTime / 1_000_000.0;

        System.out.printf("Sequential Time: %,.3f ms%n", seqMs);
        System.out.printf("Parallel Time:   %,.3f ms%n", parMs);

        if (parTime > 0) {
            double speedup = (double) seqTime / parTime;
            System.out.printf("Speedup factor:  %.2fx%n", speedup);
        } else {
            System.out.println("Speedup factor:  N/A (parallel time is zero)");
        }

        boolean same = seqGraph.deviceNodeMap.keySet()
                .equals(parGraph.deviceNodeMap.keySet());

        if (same) {
            System.out.println("Graphs match ✓");
        } else {
            System.out.println("⚠ WARNING: Reducers do NOT produce identical graphs!");
        }

        System.out.println("==================================================");
    }
}
