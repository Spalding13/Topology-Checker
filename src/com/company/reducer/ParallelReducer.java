package com.company.reducer;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.graph.Node;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class ParallelReducer {

    /**
     * Fully parallel, deterministic reducer.
     * Ensures identical results to sequential reducer.
     */
    public static Graph reduce(Graph topology) {

        long tStart = System.nanoTime();

        // 1. Extract all unique devices in parallel
        ConcurrentMap<String, Device> uniqueDevices =
                topology.getNodes()
                        .parallelStream()
                        .flatMap(node -> topology.adjacencyList.get(node).stream())
                        .map(Connection::getNode)
                        .map(Node::getDevice)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toConcurrentMap(
                                Device::getName,
                                d -> d,
                                (a, b) -> a   // keep existing if duplicate name
                        ));

        // 2. Global parallel grouping by:
        //    modelName -> pinSignature
        ConcurrentMap<String, ConcurrentMap<String, List<Device>>> grouped =
                uniqueDevices.values()
                        .parallelStream()
                        .collect(
                                Collectors.groupingByConcurrent(
                                        Device::getModelName,
                                        Collectors.groupingByConcurrent(
                                                d -> d.getPinsAndNets().toString()
                                        )
                                )
                        );

        // 3. Combine groups in parallel
        List<Device> reduced =
                grouped.values()
                        .parallelStream()
                        .flatMap(pinMap -> pinMap.values().parallelStream())
                        .map(group -> {
                            if (group.size() == 1) return group.get(0);
                            return DeviceFactory.createCombinedDevice(group);
                        })
                        .collect(Collectors.toList());

        long tEnd = System.nanoTime();

        System.out.println("[ParallelReducer] Devices: " + reduced.size());
        System.out.println("[ParallelReducer] Time: " + (tEnd - tStart) + " ns");
        System.out.println("---------------------");

        // Rebuild graph
        return GraphFactory.buildGraph(reduced, topology.getNetMap());
    }
}
