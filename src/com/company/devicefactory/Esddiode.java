package com.company.devicefactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing an Esddiode device, extending from Device.
 */
public class Esddiode extends Device {
    private final int numberOfPins = 2;
    /**
     * Constructor for creating a single Esddiode device from a line of text.
     * @param deviceLine The line containing device information.
     */
    public Esddiode(String deviceLine) {
        super();

        String[] deviceTokens = deviceLine.split(" ");
        this.setDeviceType("D");

        extractName(deviceTokens);
        extractModel(deviceTokens, getClass().getSimpleName().toLowerCase());
        extractPinsAndNets(deviceTokens);
        extractParams(deviceTokens);
    }

    /**
     * Constructor for creating a combined Esddiode device.
     * @param device The base device object to combine into an Esddiode.
     */
    public Esddiode(Device device) {
        super(device.getDeviceType(), device.getName(), device.getModelName());

        this.getPinsAndNets().putAll(device.getPinsAndNets()); // Example of combining pins and nets
        // Additional initialization specific to Esddiode
    }

    /**
     * Extracts pins and nets specific to Esddiode from the given line of text.
     * @param deviceLine The line containing device information.
     */
    @Override
    public void extractPinsAndNets(String[] deviceLine) {
        this.getPinsAndNets().put("pos", deviceLine[1]);
        this.getPinsAndNets().put("neg", deviceLine[2]);
    }

    /**
     * Reduces parameters of the device based on a map of parameters.
     * @param params The map containing parameters to reduce.
     * @return The reduced device object.
     */
    @Override
    public Device reduceParams(Map<String, String> params) {
        // Implement reduction logic specific to Esddiode if needed
        return null;
    }



    public int getNumberOfPins() {
        return numberOfPins;
    }
}
