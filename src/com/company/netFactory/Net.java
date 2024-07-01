package com.company.netFactory;

import com.company.devicefactory.Device;

import java.util.ArrayList;
import java.util.List;

public class Net {

    private String name;
    private List<String> types;
    private List<Device> deviceList;

    public Net (String name) {

        this.name = name;
        this.types = new ArrayList<>();
        this.deviceList = new ArrayList<>();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTypes () {return this.types;}

    public List<Device> getDevicesConnectedToNet () {return this.deviceList;}

    private void setNetType(String netType) {

        if(!types.contains(netType)) {
            this.types.add(netType);
        }
    }

    public void setDevicesConnectedToNet (Device device) {
        if(!this.deviceList.contains(device)) {
            this.setNetType(device.getDeviceType());
            this.deviceList.add(device);
        }
    }

    private String convertObjectArrayToString(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : list)
            sb.append(obj.toString()).append(delimiter);
        return sb.substring(0, sb.length() - 1);

    }
}
