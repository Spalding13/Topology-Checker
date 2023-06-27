package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

public class Connection {

    public Node node;
    public String pin;

    public Connection (Node node, String pin) {
        this.node = node;
        this.pin = pin;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.node.toString() + " : " + this.pin);

        return stringBuilder.toString();

    }
}
