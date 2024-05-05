package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class GraphFactory {

    private static List<Net> nets;
    private static CopyOnWriteArrayList<Device> devices;

    // Helper method that associate each device to each net
    private static void tieNetsToDevices(List<Net> nets, List<Device> devices) {

        for (Device device: devices) {

            device.pinsAndNets.forEach((pin, net)-> {
                Net netObj = NetFactory.getNetByName(net);
                netObj.setDevicesConnectedToNet(device);
                device.setPinNetMap(pin, netObj);
            });
        }

    }

    private static void transformNetlist() {

        NetlistTransformer.parallelTransform();

    }

    public static Graph buildGraph(List<Net> nets, CopyOnWriteArrayList<Device> devices) {

        GraphFactory.devices = devices;
        GraphFactory.nets = nets;

        tieNetsToDevices(GraphFactory.nets, GraphFactory.devices);
//        transformNetlist();

        Graph graph = new Graph();

        GraphFactory.devices.forEach(device -> {
            System.out.println(device.name);

            Node deviceNode = graph.addDeviceNode(device);

            System.out.println(deviceNode);

            //Device could be added already (although unlikely)
            if(deviceNode==null) return;

            device.pinNetMap.forEach((pin, net) -> {

                Node netNode = graph.addNetNode(net);

                //Net could be added already
                if(netNode==null) netNode = graph.getNetNodeByName(net);

                System.out.println("Device node->" + deviceNode);
                graph.addEdge(deviceNode, netNode, pin);
            });
        });

        graph.printGraph();
        return graph;
        }

    public static class NetlistTransformer {


        public static void parallelTransform() {

            GraphFactory.devices.forEach(device -> {

                device.pinNetMap.forEach((pin, net) -> {
                    String pinsAndNetsString = device.getPinNetMapString();
                    List<Device> devicesConnectedtoNet = net.getDevicesConnectedToNet();

                    AtomicReference<Boolean> sameModels = new AtomicReference<>(false);
                    AtomicReference<Boolean> allPinsParallel = new AtomicReference<>(false);


                    devicesConnectedtoNet.forEach(device1 -> {

                        String pinsAndNetsString1 = device1.getPinNetMapString();

                        if(device1.deviceType.equals(device.deviceType)) {
                            sameModels.set(true);
                        }

                        if(pinsAndNetsString1.equals(pinsAndNetsString)) {
                            allPinsParallel.set(true);
                        }

                        if (sameModels.get() && allPinsParallel.get()) {
                            System.out.println(device.name + " and " + device1.name + " are parallel");
                            devices.remove(device);
                            devices.remove(device1);
                        }

                    });

                });

            });

        }
        
        public void reduceDevices(Device device1, Device device2) {


        }
    }

    }