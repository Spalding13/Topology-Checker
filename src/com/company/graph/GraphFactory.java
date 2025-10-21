package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.List;
import java.util.Map;

/**
 * GraphFactory builds a graph data structure based on a netlist of devices and their connections.
 */
public class GraphFactory {

    /**
     * Builds the graph data structure.
     *
     * @param devices List of devices
     * @param netMap  Map of net names to Net objects
     * @return The constructed graph
     */
    public static Graph buildGraph(List<Device> devices, Map<String, Net> netMap) {
        // Create an empty graph
        Graph graph = new Graph();

        // Associate each device with all associated net objects
        tieNetsToDevices(devices, netMap);

        // Add device and net nodes to the graph and establish connections
        addNodesAndEdges(graph, devices);

        // Print the graph for debugging purposes
        graph.printGraph();

        return graph;
    }

    /**
     * Associates each device with its connected net objects.
     *
     * @param devices List of devices
     * @param netMap  Map of net names to Net objects
     */
    private static void tieNetsToDevices(List<Device> devices, Map<String, Net> netMap) {
        for (Device device : devices) {
            Map<String, String> pinsAndNets = device.getPinsAndNets();

            for (String pin : pinsAndNets.keySet()) {
                String netName = pinsAndNets.get(pin);
                Net netObj = netMap.get(netName); // Lookup the Net by name

                if (netObj != null) {
                    netObj.setDevicesConnectedToNet(device);
                    device.setPinNetMap(pin, netObj);
                } else {
                    System.err.println("Warning: Net not found for name: " + netName);
                }
            }
        }
    }

    /**
     * Adds device and net nodes to the graph and establishes connections between them.
     *
     * @param graph   The graph to which nodes and edges will be added
     * @param devices List of devices
     */
    private static void addNodesAndEdges(Graph graph, List<Device> devices) {
        for (Device device : devices) {
            // Add device node to the graph
            Node deviceNode = graph.addDeviceNode(device);

            if (deviceNode != null) {
                for (String pin : device.getPinNetMap().keySet()) {
                    // Add net node to the graph
                    Net net = device.getPinNetMap().get(pin);
                    Node netNode = graph.addNetNode(net);

                    // Establish connection between device and net nodes
                    if (netNode == null) netNode = graph.getNetNodeByName(net);
                    graph.addEdge(deviceNode, netNode, pin);
                }
            } else {
                System.err.println("Warning: Device node already added! " + device.getName());
            }
        }
    }
}
