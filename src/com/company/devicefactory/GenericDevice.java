package com.company.devicefactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GenericDevice is a fallback class used when a specific device model class
 * is not found. It safely extends Device and implements its abstract methods
 * in a neutral, generic way.
 */
public class GenericDevice extends Device {

    /**
     * Constructor for creating a generic device from a CDL line.
     * Used when no specific device implementation exists for the model.
     *
     * @param deviceLine The CDL line describing the device.
     * @param modelName  The extracted model name.
     */
    public GenericDevice(String deviceLine, String modelName) {
        super();

        String[] tokens = deviceLine.trim().split("\\s+");
        this.setDeviceType("X"); // generic placeholder type
        this.setModelName(modelName);
        extractName(tokens);
        extractPinsAndNets(tokens);
        extractParams(tokens);
    }

    /**
     * Copy constructor for combined devices.
     * Used during reduction when merging multiple devices.
     */
    public GenericDevice(Device device) {
        super(device.getDeviceType(), device.getName(), device.getModelName());
        this.getPinsAndNets().putAll(device.getPinsAndNets());
        this.setParams(new HashMap<>(device.getParams()));
    }

    @Override
    public void extractPinsAndNets(String[] tokens) {
        // Generic extraction: assume all tokens except the first (name) and model are nets
        Map<String, String> pinsAndNets = new HashMap<>();

        if (tokens.length < 3) return;

        // The model is typically the last token before parameters (before '=' appears)
        int modelIndex = tokens.length;
        for (int i = 1; i < tokens.length; i++) {
            if (tokens[i].contains("=")) {
                modelIndex = i - 1;
                break;
            }
        }

        for (int i = 1; i < modelIndex; i++) {
            pinsAndNets.put("PIN" + i, tokens[i]);
        }

        this.setPinsAndNets(pinsAndNets);
    }

    @Override
    public Map<String, String> recalculateParallelParams(List<Map<String, String>> params) {
        // Default generic behavior: combine numeric params additively where possible
        Map<String, String> combined = new HashMap<>();

        for (Map<String, String> paramMap : params) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                try {
                    double existing = combined.containsKey(key)
                            ? Double.parseDouble(combined.get(key))
                            : 0.0;
                    combined.put(key, Double.toString(existing + Double.parseDouble(value)));
                } catch (NumberFormatException e) {
                    // If not numeric, just keep the latest non-numeric value
                    combined.put(key, value);
                }
            }
        }

        return combined;
    }
}
