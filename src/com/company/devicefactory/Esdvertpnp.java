package com.company.devicefactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing an Esdvertpnp device, extending from Device.
 */
public class Esdvertpnp extends Device {
    private final int numberOfPins = 3;
    /**
     * Constructor for creating an Esdvertpnp device from a line of text.
     * @param deviceLine The line containing device information.
     */
    public Esdvertpnp(String deviceLine) {
        super();

        String[] deviceTokens = deviceLine.split(" ");
        this.setDeviceType("Q");

        extractName(deviceTokens);
        extractModel(deviceTokens, getClass().getSimpleName().toLowerCase());
        extractPinsAndNets(deviceTokens);
        extractParams(deviceTokens);
    }

    /**
     * Constructor for creating a combined Esdvertpnp device.
     * @param device The base device object to combine into an Esdvertpnp.
     */
    public Esdvertpnp(Device device) {
        super(device.getDeviceType(), device.getName(), device.getModelName());


        this.getPinsAndNets().putAll(device.getPinsAndNets()); // Example of combining pins and nets
        // Additional initialization specific to Esdvertpnp
    }

    /**
     * Extracts pins and nets specific to Esdvertpnp from the given line of text.
     * @param deviceLine The line containing device information.
     */
    @Override
    public void extractPinsAndNets(String[] deviceLine) {
        this.getPinsAndNets().put("e", deviceLine[1]);
        this.getPinsAndNets().put("b", deviceLine[2]);
        this.getPinsAndNets().put("c", deviceLine[3]);
    }

    /**
     * Reduces parameters of the device based on a map of parameters.
     * @param params The map containing parameters to reduce.
     * @return The reduced device object.
     */
    @Override
    public Device reduceParams(Map<String, String> params) {
        // Implement reduction logic specific to Esdvertpnp if needed
        return null;
    }
}
