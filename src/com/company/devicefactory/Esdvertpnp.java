package com.company.devicefactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    public Map<String, String> recalculateParallelParams(List<Map<String, String>> params) {
        // Initialize accumulators
        AtomicInteger totalNf = new AtomicInteger();
        double totalAreapd = 0.0;
        double totalPerimpd = 0.0;

        // Create a new map for the reduced parameters
        Map<String, String> reducedParams = new HashMap<>();

        // Sum up the values
        for (Map<String, String> paramMap : params) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Add more cases if there are other parameters to reduce
                switch (key) {
                    case "nf" -> totalNf.addAndGet(Integer.parseInt(value));
                    case "areapd" -> totalAreapd += Double.parseDouble(value);
                    case "perimpd" -> totalPerimpd += Device.parseScientificNotation(value);
                    default -> reducedParams.put(key, value);
                }
            }
        }


        reducedParams.put("nf", Integer.toString(totalNf.get()));
        reducedParams.put("areapd", Double.toString(totalAreapd));
        reducedParams.put("perimpd", Double.toString(totalPerimpd));

        return reducedParams;
    }
}
