package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.graph.Node;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**The Reducer class is responsible
 * for reducing the complexity of a Graph
 * by identifying and combining parallel devices
 * into a single representative device.
 *
 * This is particularly useful for the
 * topology analysis tool, where large datasets
 * need to be processed efficiently.**/
public class Reducer {

    public static void reduce(Graph topology) {
        // Implementation for reduce method
        parallelReduce(topology);
    }

    private static void parallelReduce(Graph topology) {
        // Use a thread-safe set for tracking visited devices
        ConcurrentMap<Device, Boolean> visitedDevices = new ConcurrentHashMap<>();

        long startTime = System.nanoTime();

        // Create a thread-safe list for storing reduced devices
        Graph finalTopology_temp  = topology;
        List<Device> reducedDevices = topology.netNodeMap.values().parallelStream()
                .flatMap(netNode -> {
                    // Retrieve connections for each net node
                    List<Connection> connections = finalTopology_temp.adjacencyList.get(netNode);
                    System.out.println("Exploring devices connected to net node: " + netNode);

                    // Explore and group devices in parallel
                    List<List<Device>> parallelDeviceGroups = exploreParallelDevices(connections, visitedDevices);

                    // Combine parallel devices
                    return combineParallelDevices(parallelDeviceGroups).stream();
                })
                .collect(Collectors.toList()); // Collect all results into a single list

        System.out.println(finalTopology_temp.adjacencyList.size());

        long endTime = System.nanoTime();
        long durationStream = endTime - startTime;
        System.out.println("Time taken with parallelStream(): " + durationStream + " nanoseconds");


        // Rebuild the graph with reduced devices
        topology = GraphFactory.buildGraph(reducedDevices);
    }

    private static List<List<Device>> exploreParallelDevices(List<Connection> connections, ConcurrentMap<Device, Boolean> visited) {
        // Group devices by ModelName and PinsAndNets concurrently
        ConcurrentMap<String, ConcurrentMap<String, List<Device>>> groupedDevices = connections.parallelStream()
                .map(Connection::getNode)
                .map(Node::getDevice)
                .filter(device -> device != null && visited.putIfAbsent(device, Boolean.TRUE) == null) // Filter and mark as visited
                .collect(Collectors.groupingByConcurrent(
                        Device::getModelName,
                        Collectors.groupingByConcurrent(device -> device.getPinsAndNets().toString())
                ));

        // Flatten the grouped devices into a list of device groups
        return groupedDevices.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .collect(Collectors.toList());
    }

    private static List<Device> combineParallelDevices(List<List<Device>> parallelGroups) {
        // Logic to combine parallel devices into one representative device
        // For example, merge attributes or take an average of values
        // This logic will depend on the specific requirements of your application
        // Return the combined representative device

        List<Device> reducedDevices = new ArrayList<>();

        // Iterate through each group
        for (List<Device> group : parallelGroups) {

            // A single device can't be reduced by the next algorithms
            // add it to the collection anyway and skip to next iteration
            if (group.size() == 1){
                reducedDevices.add(group.get(0));
                continue;
            }

            // Use the first device in the group to determine the device type
            if (!group.isEmpty()) {
                Device firstDevice = group.get(0);

                String deviceType = firstDevice.getDeviceType();

                // Use the factory class to create a combined device of the same type
                Device combinedDevice = DeviceFactory.createCombinedDevice(group, deviceType);


                reducedDevices.add(combinedDevice);
            }
        }

        return reducedDevices;
    }

    private static void calculateParameters(Device device) {
        // Implement logic to calculate additional parameters for the representative device
        // This could involve computing parameters based on the combined attributes of the devices
    }


    private static String convertPropsToCriteria(Node node) {
        // Build a criteria for parallelism
        String connectionsString = node.device.getPinNetMapString();
        String name = node.device.getModelName();

        return name + "_" + connectionsString;
    }

    private static void processParallelDevices(List<Node> parallelDevices) {
        // Process the list of parallel devices (e.g., combine them into one)
        // Implement your logic here based on the requirements
    }

    private static String convertConnectionsToString(List<Connection> connections) {
        return connections.stream()
                .map(Connection::toString)
                .collect(Collectors.joining());
    }

    private static boolean equalTypes(String type1, String type2) {

        return type1.equals(type2);
    }

    private static boolean detectSeries(Device device) {
        // Implementation for detectSeries method
        return true;
    }

}