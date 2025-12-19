package com.company.reducer_benchmark;

import com.company.graph.Graph;
import com.company.graph.Node;
import com.company.reducer.ReducerUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sequential (single-threaded) reducer.
 */
public class SequentialReducer {

    public static Graph reduce(Graph topology) {
        long start = System.nanoTime();

        Set<String> visited = new HashSet<>();
        List<Node> nets = new ArrayList<>(topology.netNodeMap.values());
        List<com.company.devicefactory.Device> reduced = new ArrayList<>();

        for (Node net : nets) {
            reduced.addAll(
                    ReducerUtils.collectFromNetNode(topology, net, visited)
            );
        }

        Graph newGraph = com.company.graph.GraphFactory.buildGraph(
                reduced,
                topology.getNetMap()
        );

        long end = System.nanoTime();
        System.out.printf("[SequentialReducer] Devices: %d%n", reduced.size());
        System.out.printf("[SequentialReducer] Time: %d ns%n", (end - start));
        return newGraph;
    }
}
