package com.company.netFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetFactory {

    public static HashMap<String, Net> netByName = new HashMap<>();
    public static HashMap<String, Net> netByNetType = new HashMap<>();
    public static HashMap<String, Net> netByPathType = new HashMap<>();

    public static List<Net> createNets (List<String> netsList) {

        List<Net> nets = new ArrayList<>();

        for (String line: netsList) {
            Net net = new Net(line);

            netByName.put(line, net);

            nets.add(net);
        }

        return nets;
    }

    public static Net getNetByName (String name) {
        return netByName.get(name);
    }
}
