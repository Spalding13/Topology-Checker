package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.*;

/**
 * Represents a graph data structure composed of nodes and edges.
 */
public class Graph {

    // Maps to store nodes by name with non-thread-safe implementations
    public Map<String, Node> deviceNodeMap = new HashMap<>();
    public Map<String, Node> netNodeMap = new HashMap<>();

    // Adjacency list to store connections between nodes
    public Map<Node, List<Connection>> adjacencyList = new HashMap<>();

    /**
     * Constructs a new Graph object.
     */
    public Graph() {
        // No need to initialize adjacencyList in the constructor, it's already done above
    }

    /**
     * Adds a device node to the graph.
     * @param device The device to be added as a node.
     * @return The newly added node if successful, or null if the device already exists in the graph.
     */
    public Node addDeviceNode(Device device) {
        Node newNode = new Node(device, null);
        return deviceNodeMap.computeIfAbsent(device.getName(), k -> {
            List<Connection> connectionsList = new ArrayList<>();
            adjacencyList.put(newNode, connectionsList);
            return newNode;
        });
    }

    /**
     * Adds a net node to the graph.
     * @param net The net to be added as a node.
     * @return The newly added node if successful, or null if the net already exists in the graph.
     */
    public Node addNetNode(Net net) {
        Node newNode = new Node(null, net);
        return netNodeMap.computeIfAbsent(net.getName(), k -> {
            List<Connection> connectionsList = new ArrayList<>();
            adjacencyList.put(newNode, connectionsList);
            return newNode;
        });
    }

    /**
     * Adds an edge between two nodes in the graph.
     * @param n1 The first node.
     * @param n2 The second node.
     * @param pin The pin connecting the nodes.
     */
    public void addEdge(Node n1, Node n2, String pin) {
        adjacencyList.computeIfAbsent(n1, k -> new ArrayList<>())
                .add(new Connection(n2, pin));
        adjacencyList.computeIfAbsent(n2, k -> new ArrayList<>())
                .add(new Connection(n1, pin));
    }

    /**
     * Retrieves the net node associated with the given net.
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
        List<Connection> connections1 = adjacencyList.get(n1);
        List<Connection> connections2 = adjacencyList.get(n2);
        if (connections1 != null) {
            connections1.removeIf(connection -> connection.getNode().equals(n2) && connection.getPin().equals(pin));
        }
        if (connections2 != null) {
            connections2.removeIf(connection -> connection.getNode().equals(n1) && connection.getPin().equals(pin));
        }
    }

    /**
     * Removes a device node and its associated connections from the graph.
     * @param device The device to be removed.
     * @return The removed node if successful, or null if the device does not exist in the graph.
     */
    public Node removeDeviceNode(Device device) {
        Node nodeToRemove = deviceNodeMap.remove(device.getName());
        if (nodeToRemove == null) return null;
        adjacencyList.remove(nodeToRemove);
        removeConnections(nodeToRemove);
        return nodeToRemove;
    }

    /**
     * Removes a net node and its associated connections from the graph.
     * @param net The net to be removed.
     * @return The removed node if successful, or null if the net does not exist in the graph.
     */
    public Node removeNetNode(Net net) {
        Node nodeToRemove = netNodeMap.remove(net.getName());
        if (nodeToRemove == null) return null;
        adjacencyList.remove(nodeToRemove);
        removeConnections(nodeToRemove);
        return nodeToRemove;
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

    /**
     * Prints the graph structure.
     */
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
