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
     * Retrieves all nodes in the graph.
     * @return A collection of all nodes in the graph.
     */
    public Collection<Node> getNodes() {
        Set<Node> allNodes = new HashSet<>();
        allNodes.addAll(deviceNodeMap.values());
        allNodes.addAll(netNodeMap.values());
        return allNodes;
    }

    /**
     * Retrieves the connections associated with a specific node.
     *
     * @param node The node whose connections are to be retrieved.
     * @return A list of connections associated with the node, or an empty list if none are found.
     */
    public List<Connection> getConnections(Node node) {
        return adjacencyList.getOrDefault(node, Collections.emptyList());
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


    /**
     * Retrieves a mapping of net names to their corresponding Net objects
     * from all net nodes in the graph.
     *
     * @return A map where the key is the net name (String) and the value is the Net object.
     */
    public Map<String, Net> getNetMap() {
        Map<String, Net> netMap = new HashMap<>();
        for (Map.Entry<String, Node> entry : netNodeMap.entrySet()) {
            Node node = entry.getValue();
            if (node != null && node.getNet() != null) {
                netMap.put(entry.getKey(), node.getNet());
            }
        }
        return netMap;
    }

    public Graph deepCopy(Map<String, Net> netMap) {
        Graph copy = new Graph();

        Map<Node, Node> nodeMap = new HashMap<>();

        // --- 1. Copy nodes ---
        for (Node original : this.getNodes()) {

            Node newNode;

            // Device node
            if (original.getDevice() != null) {
                Device origDev = original.getDevice();
                Device newDev;

                try {
                    newDev = origDev.getClass()
                            .getConstructor(Device.class)
                            .newInstance(origDev);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to copy device: " + origDev.getName(), e);
                }

                copy.addDeviceNode(newDev);
                newNode = copy.deviceNodeMap.get(newDev.getName());
            }

            // Net node
            else {
                Net origNet = original.getNet();
                Net mappedNet = netMap.get(origNet.getName());

                copy.addNetNode(mappedNet);
                newNode = copy.netNodeMap.get(mappedNet.getName());
            }

            nodeMap.put(original, newNode);
        }

        // --- 2. Copy adjacency list ---
        for (Map.Entry<Node, List<Connection>> entry : this.adjacencyList.entrySet()) {
            Node originalNode = entry.getKey();
            Node newNode = nodeMap.get(originalNode);

            for (Connection conn : entry.getValue()) {
                Node origNeighbor = conn.getNode();
                Node newNeighbor = nodeMap.get(origNeighbor);

                copy.addEdge(newNode, newNeighbor, conn.getPin());
            }
        }

        return copy;
    }

    // For Benchmarking and comparison  purposes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph other)) return false;

        return this.deviceNodeMap.keySet().equals(other.deviceNodeMap.keySet()) &&
                this.netNodeMap.keySet().equals(other.netNodeMap.keySet()) &&
                adjacencyEquals(this.adjacencyList, other.adjacencyList);
    }

    private boolean adjacencyEquals(Map<Node, List<Connection>> a,
                                    Map<Node, List<Connection>> b) {
        if (a.size() != b.size()) return false;

        for (Node n : a.keySet()) {
            List<Connection> listA = a.get(n);
            List<Connection> listB = b.get(n);
            if (listB == null) return false;

            if (listA.size() != listB.size()) return false;

            // Connection order doesn't matter -> compare as sets
            if (!new HashSet<>(listA).equals(new HashSet<>(listB))) return false;
        }

        return true;
    }


}
