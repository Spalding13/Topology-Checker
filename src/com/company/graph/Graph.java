package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.*;

public class Graph {

    public Map<String, Node> deviceNodeMap = new HashMap<>();
    public Map<String, Node>    netNodeMap    = new HashMap<>();

    private Map<Node, List<Connection>> adjacencyList;

    public Graph() {
        this.adjacencyList = new LinkedHashMap<>();
    }

    public Node addDeviceNode(Device device){

        if(deviceNodeMap.containsKey(device.name)) return null;

        //Add Device type node
        Node newNode = new Node(device, null);
        deviceNodeMap.put(device.name, newNode);

        List<Connection> connectionsList = new ArrayList<>();
        this.adjacencyList.put(newNode, connectionsList);

        return newNode;
    }

    public Node addNetNode(Net net){

        if(netNodeMap.containsKey(net.name)) return null;

        //Add Net type node
        Node newNode = new Node(null, net);
        netNodeMap.put(net.name, newNode);


        List<Connection> connectionsList = new ArrayList<>();
        this.adjacencyList.put(newNode, connectionsList);

        return newNode;
    }

    public void addEdge(Node n1, Node n2, String pin) {

        Connection connection1 = new Connection(n2, pin);
        this.adjacencyList.get(n1).add(connection1);

        Connection connection2 = new Connection(n1, pin);
        this.adjacencyList.get(n2).add(connection2);

    }

    public Node getDeviceNode(Device device) {

        return this.deviceNodeMap.get(device);

    }


    public Node getNetNodeByName(Net net) {

        System.out.println("Getting node with net name " + net.name);
        return this.netNodeMap.get(net.name);

    }

    public Node getDeviceNodeByName(Device device) {

        System.out.println("Getting node with net name " + device.name);
        return this.deviceNodeMap.get(device.name);

    }

    public void printGraph(){
        System.out.println("\n--------Graph--------: \n");
        for (Map.Entry<Node, List<Connection>> vertexKey : this.adjacencyList.entrySet()) {
            System.out.printf("%s = [", vertexKey.getKey().toString());
            for (int i = 0; i < vertexKey.getValue().size(); i++) {
                String nodeName = vertexKey.getValue().get(i).toString();

                System.out.printf("{ %s }", nodeName);
            }
            System.out.print("]");
            System.out.println();
        }
        System.out.println("\n---------------------");
    }


}