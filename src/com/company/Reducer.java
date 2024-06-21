package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reducer {

    public static void reduce(Graph topology) {
        // Implementation for reduce method
        parallelReduce(topology);
    }

    private static void parallelReduce(Graph topology) {
        // Initialize a set to track visited net nodes during traversal
        Set<Node> visited = new HashSet<>();

        // Iterate over all net nodes in the graph
        for (Node netNode : topology.netNodeMap.values()) {

            // Perform parallel reduction from each unvisited net node
            if (!visited.contains(netNode)) {

                // Explore parallel devices connected to the current net node
                List<List<Device>> parallelDeviceGroups = exploreParallelDevices(topology, netNode, visited);

                // Process parallel devices found during traversal
                List<Device> reducedDevices = combineParallelDevices(parallelDeviceGroups);

                System.out.println(parallelDeviceGroups);
                System.out.println(reducedDevices);
                System.out.println("Parallel and Reduced devices:");
            }
        }
    }

    private static List<List<Device>> exploreParallelDevices(Graph topology, Node netNode, Set<Node> visited) {
        System.out.println("Exploring devices connected to net node: " + netNode.toString());

        // Retrieve connections of the current net node
        List<Connection> connections = topology.adjacencyList.get(netNode);

        // Group devices by model name and pinsAndNets
        Map<String, Map<String, List<Device>>> groupedDevices = connections.stream()
                .flatMap(connection -> Stream.of(connection.getNode()))
                .map(Node::getDevice)
                .collect(Collectors.groupingBy(Device::getModelName, // Group by model name
                        Collectors.groupingBy(device -> device.getPinsAndNets().toString()))); // Group by pinsAndNets

        // Extract the values (list of devices) from the nested map
        List<List<Device>> separatedGroups = groupedDevices.values().stream()
                .flatMap(innerMap -> innerMap.values().stream()) // Flatten the inner map values
                .collect(Collectors.toList());

        // Print the separated groups of devices
        for (List<Device> group : separatedGroups) {
            System.out.println("Group:");
            for (Device device : group) {
                System.out.println(device.getModelName() + " - " + device.getName());
            }
        }

        return separatedGroups;
    }

    private static List<Device> combineParallelDevices(List<List<Device>> parallelGroups) {
        // Implement logic to combine parallel devices into one representative device
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