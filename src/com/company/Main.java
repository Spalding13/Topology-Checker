package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.company.graph.GraphFactory.buildGraph;

public class Main {

    public static void main(String[] args) throws IOException {

        List<String> ports = Arrays.asList("PAD", "VDD<1>", "GND<1>", "GND<2>");


        String input = NetlistReader.openFile("E:\\ESD Checks\\input\\netlist.cdl");

        System.out.println(input);

        // netlistInfo => "devices", "nets", "designDetails"
        Map<String, List<String>> netlistInfo = StateMachine.parseNetlist(input);

        netlistInfo.forEach((key, value) -> {
            System.out.println(key + " -> " + value);
        });

        List<Net> nets = NetFactory.createNets(netlistInfo.get("nets"));
        CopyOnWriteArrayList<Device> devices = DeviceFactory.createDevices(netlistInfo.get("devices"));

        List<String> designDetails = netlistInfo.get("designDetails");

        Graph graph = GraphFactory.buildGraph(nets, devices);

//        for (Net net: nets){
//            System.out.println(" PRINTING NETS -> " + net.name);
//        }
//
//        for (Device device: devices){
//            System.out.println(" PRINTING DEVICES -> " + device.deviceName + device.params);
//        }
    }
}
