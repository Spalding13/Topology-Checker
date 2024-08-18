package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.graph.Node;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**The Reducer class is responsible
 * for reducing the complexity of a Graph
 * by identifying and combining parallel devices
 * into a single representative device.
 *
 * This is particularly useful for the
 * topology analysis tool, where large datasets
 * need to be processed efficiently.**/
public class Reducer {

    public static Graph reduce(Graph topology) {
        // Implementation for parallel reduce method
        topology = parallelReduce(topology);

        return topology;
    }

    public static Graph parallelReduce(Graph topology) {
        long startTime = System.nanoTime();

        // Perform local reductions in parallel for each NetNode
        // Here use either parallel stream or regular stream of nets

        Set<String> visitedDevices = ConcurrentHashMap.newKeySet();

        Graph topology_temp = topology;
        List<Device> reducedDevices =
                topology.netNodeMap.values().stream()
                        .flatMap(netNode -> parallelCollector(topology_temp, netNode, visitedDevices).stream())
                        //.distinct() // Ensure devices are unique. Can be explored in future version
                        .collect(Collectors.toList());

        // Benchmark performance
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        // Directly update topology
        topology = GraphFactory.buildGraph(reducedDevices);

        System.out.printf("Total amount of devices: %d%n", reducedDevices.size());
        System.out.println("Time taken with parallel parallelCollector(): " + duration + " nanoseconds");

        return topology;
    }

    private static List<Device> parallelCollector(Graph topology, Node netNode, Set<String> visitedDevices) {
        // Retrieve the list of connections for the given net node
        List<Connection> connections = topology.adjacencyList.get(netNode);

        // Collect devices, ensuring they haven't been visited before
        List<Device> localDevices = connections.stream()
                .map(Connection::getNode)
                .map(Node::getDevice)
                .filter(device -> device != null && visitedDevices.add(device.getName()))
                .collect(Collectors.toList());

        // Group devices by model name and pins/nets
        Map<String, Map<String, List<Device>>> groupedDevices = localDevices.stream()
                .collect(Collectors.groupingBy(
                        Device::getModelName,
                        Collectors.groupingBy(
                                device -> device.getPinsAndNets().toString(),
                                Collectors.toList()
                        )
                ));

        // Combine and return devices within each group
        return combineParallelDevices(groupedDevices.values().stream()
                .flatMap(pinGroups -> pinGroups.values().stream())
                .collect(Collectors.toList()));
    }


    private static List<Device> combineParallelDevices(List<List<Device>> parallelGroups) {
        return parallelGroups.stream().map(group -> {
            if (group.size() == 1) {
                return group.get(0);
            } else {
                // Use the first device in the group to determine the device type
                Device firstDevice = group.get(0);
                String deviceType = firstDevice.getDeviceType();

                // Use the factory class to create a combined device of the same type
                return DeviceFactory.createCombinedDevice(group, deviceType);
            }
        }).collect(Collectors.toList());
    }
}