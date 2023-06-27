package com.company.devicefactory;

import javax.management.RuntimeErrorException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeviceFactory {


    public static Map<String, List<Device>> deviceByType = new HashMap<>();
    public static Map<String, Device> deviceByName = new HashMap<>();

    private static Map<String, List<Device>> initDeviceByType () {
        deviceByType.put("esddiode", new ArrayList<Device>());
        deviceByType.put("esdvertpnp", new ArrayList<Device>());

        return deviceByType;
    }

    public static CopyOnWriteArrayList<Device> createDevices (List<String> deviceLines) {


        CopyOnWriteArrayList<Device> devices = new CopyOnWriteArrayList<>();
        Map<String, List<Device>> deviceByType = initDeviceByType();


        deviceLines.parallelStream().forEach(line -> {


            System.out.println("Creating ->" + line);

            if (line.contains("esddiode")) {

                Esddiode esddiode = new Esddiode(line);

                deviceByType.get("esddiode").add(esddiode);
                deviceByName.put(esddiode.name, esddiode);

                devices.add(esddiode);

            } else if (line.contains("esdvertpnp")) {

                Esdvertpnp esdvertpnp = new Esdvertpnp(line);

                deviceByType.get("esddiode").add(esdvertpnp);
                deviceByName.put(esdvertpnp.name, esdvertpnp);


                devices.add(esdvertpnp);
            }

        });

        return devices;
    }
}