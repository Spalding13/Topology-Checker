package com.company.graph;

import com.company.devicefactory.Device;
import com.company.graph.Connection;
import com.company.graph.Node;
import com.company.netFactory.Net;

import java.util.*;

/**
 * Represents a graph data structure composed of nodes and edges.
 */
public class Graph {

    // Maps to store nodes by name
    public Map<String, Node> deviceNodeMap = new HashMap<>();
    public Map<String, Node> netNodeMap = new HashMap<>();

    // Adjacency list to store connections between nodes
    public Map<Node, List<Connection>> adjacencyList;

    /**
     * Constructs a new Graph object.
     */
    public Graph() {
        this.adjacencyList = new LinkedHashMap<>();
    }

    /**
     * Adds a device node to the graph.
     * @param device The device to be added as a node.
     * @return The newly added node if successful, or null if the device already exists in the graph.
     */
    public Node addDeviceNode(Device device) {
        if (deviceNodeMap.containsKey(device.getName())) return null;
        Node newNode = new Node(device, null);
        deviceNodeMap.put(device.getName(), newNode);
        List<Connection> connectionsList = new ArrayList<>();
        this.adjacencyList.put(newNode, connectionsList);
        return newNode;
    }

    /**
     * Adds a net node to the graph.
     * @param net The net to be added as a node.
     * @return The newly added node if successful, or null if the net already exists in the graph.
     */
    public Node addNetNode(Net net) {
        if (netNodeMap.containsKey(net.getName())) return null;
        Node newNode = new Node(null, net);
        netNodeMap.put(net.getName(), newNode);
        List<Connection> connectionsList = new ArrayList<>();
        this.adjacencyList.put(newNode, connectionsList);
        return newNode;
    }

    /**
     * Adds an edge between two nodes in the graph.
     * @param n1 The first node.
     * @param n2 The second node.
     * @param pin The pin connecting the nodes.
     */
    public void addEdge(Node n1, Node n2, String pin) {
        Connection connection1 = new Connection(n2, pin);
        this.adjacencyList.get(n1).add(connection1);
        Connection connection2 = new Connection(n1, pin);
        this.adjacencyList.get(n2).add(connection2);
    }

    /**
     * Retrieves the net node associated with the given net.
     *
     * @param net The net.
     * @return The net node, or null if not found.
     */
    public Node getNetNodeByName(Net net) {
        return this.netNodeMap.get(net.getName());
    }

    /**
     * Removes an edge between two nodes in the graph.
     * @param n1 The first node.
     * @param n2 The second node.
     * @param pin The pin connecting the nodes.
     */
    public void removeEdge(Node n1, Node n2, String pin) {
        List<Connection> connections1 = this.adjacencyList.get(n1);
        List<Connection> connections2 = this.adjacencyList.get(n2);
        connections1.removeIf(connection -> connection.getNode().equals(n2) && connection.getPin().equals(pin));
        connections2.removeIf(connection -> connection.getNode().equals(n1) && connection.getPin().equals(pin));
    }

    /**
     * Removes a device node and its associated connections from the graph.
     * @param device The device to be removed.
     * @return The removed node if successful, or null if the device does not exist in the graph.
     */
    public Node removeDeviceNode(Device device) {
        Node nodeToRemove = deviceNodeMap.get(device.getName());
        if (nodeToRemove == null) return null;
        adjacencyList.remove(nodeToRemove);
        removeConnections(nodeToRemove);

        return deviceNodeMap.remove(device.getName());
    }

    /**
     * Removes a net node and its associated connections from the graph.
     * @param net The net to be removed.
     * @return The removed node if successful, or null if the net does not exist in the graph.
     */
    public Node removeNetNode(Net net) {
        Node nodeToRemove = netNodeMap.get(net.getName());
        if (nodeToRemove == null) return null;
        adjacencyList.remove(nodeToRemove);
        removeConnections(nodeToRemove);
        return netNodeMap.remove(net.getName());
    }

    /**
     * Removes all connections associated with a specific node.
     * @param node The node whose connections are to be removed.
     */
    private void removeConnections(Node node) {
        for (List<Connection> connections : adjacencyList.values()) {
            connections.removeIf(connection -> connection.getNode().equals(node));
        }
    }
    public void printGraph() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n--------Graph--------: \n");
        for (Map.Entry<Node, List<Connection>> vertexKey : this.adjacencyList.entrySet()) {
            stringBuilder.append(vertexKey.getKey().toString()).append(" = [");
            for (int i = 0; i < vertexKey.getValue().size(); i++) {
                String nodeName = vertexKey.getValue().get(i).toString();
                stringBuilder.append("{ ").append(nodeName).append(" }");
            }
            stringBuilder.append("]\n");
        }
        stringBuilder.append("\n---------------------");
        System.out.println(stringBuilder.toString());
    }
    // Other methods...
}
