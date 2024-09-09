package com.company;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.esd.rule.EsdaRule;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.io.IOException;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {

        // Enlist all available ports
        List<String> ports = Arrays.asList("PAD", "VDD<1>", "GND<1>", "GND<2>");
        // Read .cdl input file in to a string
        String input = NetlistReader.openFile("E:\\ESD Checks\\input\\netlist.cdl");

        // Parse input string
        // netlistInfo => "devices", "nets", "designDetails"
        StateMachine stateMachine = new StateMachine();
        Map<String, List<String>> netlistInfo = stateMachine.parseNetlist(input);
        // Object creation section
        List<Net> nets = NetFactory.createNets(netlistInfo.get("nets"));

        List<Device> devices = DeviceFactory.createDevicesFromLines(netlistInfo.get("devices"));

        List<String> designDetails = netlistInfo.get("designDetails");

        Graph graph = GraphFactory.buildGraph(devices);

        graph = Reducer.reduce(graph);

        EsdaRule rule = new EsdaRule();

        System.out.println("test");



//        For testing purposes

//        for (Net net: nets){
//            System.out.println(" PRINTING NETS -> " + net.name);
//        }
//
//        for (Device device: devices){
//            System.out.println(" PRINTING DEVICES -> " + device.deviceName + device.params);
//        }

        graph.printGraph();
    }
}
