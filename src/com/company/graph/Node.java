package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.Map;
import java.util.Objects;

public class Node {

    private Object vertex;
    private String type;
    public Device device;
    public Net net;

    private Map<Object, String> connection;

    public Node(Device device, Net net){
        if (device!= null) setType("Device");
        if (net!=null)     setType("Net");

        this.device = device;
        this.net = net;
    }

    public Node() { }

    public void setDevice(Device device) {
        this.device = device;
        if (device != null) this.type = "Device";
    }

    public void setNet(Net net) {
        this.net = net;
        if (net != null) this.type = "Net";
    }


    public Object getVertex() {
        return vertex;
    }

    public Device getDevice(){
        return device;
    }

    private void setType (String type) {this.type = type;}

    public Object getValue(){

        String elementName = "";

        if (type.equals("Device")){
            return this.device;
        } else if (type.equals("Net")) {
            return this.net;
        }

        return elementName;
    }

    public Net getNet(){


        if (type.equals("Net")) {
            return this.net;
        }

        return null;
    }

    public String toString(){

        String elementName = "";

        if (type.equals("Device")){
            elementName = this.device.getName();
        } else if (type.equals("Net")) {
            elementName = this.net.getName();
        }

        return elementName;
    }

    // For benchmarking and comparison purposes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return Objects.equals(this.toString(), other.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

}

