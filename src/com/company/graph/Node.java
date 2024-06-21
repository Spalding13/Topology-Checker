package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.Map;

public class Node {

    private Object vertex;
    private String type;
    public Device device;
    public Net net;

    private Map<Object, String> connection;

    // Constructor -- could be better
    public Node(Device device, Net net){

        if (device!= null) setType("Device");
        if (net!=null)     setType("Net");

        this.device = device;
        this.net = net;
    }

    public Object getVertex() {
        return vertex;
    }

    public Device getDevice(){
        return device;
    }

    private void setType (String type) {this.type = type;}

    public String toString(){

        String elementName = "";

        if (type.equals("Device")){
            elementName = this.device.getName();
        } else if (type.equals("Net")) {
            elementName = this.net.getName();
        }

        return elementName;
    }

}

