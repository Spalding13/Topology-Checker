package com.company.netFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetFactory {

    // Instance fields to store nets by name, type, and path
    private HashMap<String, Net> netByName;
    private HashMap<String, Net> netByNetType;
    private HashMap<String, Net> netByPathType;

    /**
     * Constructs a new NetFactory instance.
     */
    public NetFactory() {
        netByName = new HashMap<>();
        netByNetType = new HashMap<>();
        netByPathType = new HashMap<>();
    }

    /**
     * Creates nets from a list of net names and stores them in instance maps.
     *
     * @param netsList List of net names
     * @return List of created Net objects
     */
    public List<Net> createNets(List<String> netsList) {
        List<Net> nets = new ArrayList<>();

        for (String line : netsList) {
            Net net = new Net(line);

            // Store net by name
            netByName.put(line, net);

            // Optionally, store nets by type or path if required
            // netByNetType.put(net.getType(), net);
            // netByPathType.put(net.getPath(), net);

            nets.add(net);
        }

        return nets;
    }

    /**
     * Retrieves a Net by its name.
     *
     * @param name The name of the net
     * @return The Net object or null if not found
     */
    public Net getNetByName(String name) {
        return netByName.get(name);
    }

    /**
     * Retrieves the complete map of nets by their names.
     *
     * @return Map of net names to Net objects
     */
    public Map<String, Net> getNetMap() {
        return netByName;
    }
}
