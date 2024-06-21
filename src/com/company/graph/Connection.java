package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

public class Connection {

    private Node node;

    public String pin;

    public Connection (Node node, String pin) {
        this.node = node;
        this.pin = pin;
    }


    public Node getNode() {
        return node;
    }
    public String getPin() {
        return pin;
    }

    public String toString() {
        return this.node.toString() + " : " + this.pin;
    }

}
