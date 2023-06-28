package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class GraphFactory {

    private static List<Net> nets;
    private static CopyOnWriteArrayList<Device> devices;

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
        transformNetlist();

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

                             if(device.name == device1.name){
                                System.out.println("Skipping for "+device1.name);
                             } else {
                                System.out.println(device.name + " and " + device1.name + " are parallel");
                                reduceDevices(device,device1);

                                // Nice place to start reducing/optimizing the graph ?
                            }
                        }

                    });

                });

            });

        }
        static String DeviceEqName ="XD_eq";
        static int DeviceEqNum = 0;
        public static void reduceDevices(Device device1, Device device2) {
            
                System.out.println("Reducing to equivalent netlist");
                Device device_eq = device1;
                Collection<String> device_parameters1;
                Collection<String> device_parameters2;
                devices.remove(device1);
                devices.remove(device2);
                device_parameters1 = device1.params.values();
                device_parameters2 = device2.params.values();
                Object[] params1 = device_parameters1.toArray();
                Object[] params2 = device_parameters2.toArray();
                double number1 = Double.valueOf(params1[2].toString().replaceAll("[^0-9.]", ""));
                double number2 = Double.valueOf(params2[2].toString().replaceAll("[^0-9.]", ""));
                double area_pd_eq = number1 + number2;

                number1 = Double.valueOf(params1[0].toString().replaceAll("[^0-9.]", ""));
                number2 = Double.valueOf(params2[0].toString().replaceAll("[^0-9.]", ""));

                double nf_eq = number1 + number2;
                device_eq.name = DeviceEqName+Integer.toString(DeviceEqNum);
                device_eq.params.put("nf",Double.toString(nf_eq));
                device_eq.params.put("areapd",Double.toString(area_pd_eq));
                DeviceEqNum++;
                System.out.println(device_eq);
                
                devices.add(device_eq);

        }
    }

    }