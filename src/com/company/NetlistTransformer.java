package com.company;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetlistTransformer {

    private List<Device> devices;
    private List<Net> nets;
    private Map<String, List> netlistObjects;

    public NetlistTransformer(List<Device> devices, List<Net> nets) {

        this.devices = devices;
        this.nets = nets;

        netlistObjects = new HashMap<String, List>();

        netlistObjects.put("devices", devices);
        netlistObjects.put("nets",    nets);

    }


    public void parallelTransform () {

        devices.forEach(device -> {



        });


    }
}