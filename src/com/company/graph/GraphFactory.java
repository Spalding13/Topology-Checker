package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.util.List;
import java.util.Map;

/**
 * GraphFactory builds a graph data structure based on a netlist of devices and their connections.
 */
public class GraphFactory {

    /**
     * Builds the graph data structure.
     *
     * @param nets    List of nets
     * @param devices List of devices
     * @return The constructed graph
     */
    public static Graph buildGraph(List<Net> nets, List<Device> devices) {
        // Create an empty graph
        Graph graph = new Graph();

        // Associate each device with its connected nets objects in the graph
        tieNetsToDevices(devices);

        // Add device and net nodes to the graph and establish connections
        addNodesAndEdges(graph, devices);

        // Print the graph for debugging purposes
        graph.printGraph();

        return graph;
    }

    /**
     * Associates each device with its connected nets. !This will modify the List of devices!
     *
     * @param devices List of devices
     */
    private static void tieNetsToDevices(List<Device> devices) {
        for (Device device : devices) {
            Map<String, String> pinsAndNets = device.getPinsAndNets(); // Updated to use getPinsAndNets

            for (String pin : pinsAndNets.keySet()) {
                String netName = pinsAndNets.get(pin);
                Net netObj = NetFactory.getNetByName(netName);
                netObj.setDevicesConnectedToNet(device);
                device.setPinNetMap(pin, netObj);
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
                    //Net could be added already
                    if(netNode==null) netNode = graph.getNetNodeByName(net);
                    graph.addEdge(deviceNode, netNode, pin);
                }
            }
        }
    }
}
