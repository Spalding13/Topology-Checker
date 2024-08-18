package com.company.devicefactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

        this.getPinsAndNets().putAll(device.getPinsAndNets()); // Example of combining pins and nets4
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

    @Override
    public Map<String, String> recalculateParallelParams(List<Map<String, String>> params) {
        // Initialize accumulators
        AtomicInteger totalNf = new AtomicInteger();
        double totalAreapd = 0.0;
        double totalPerimpd = 0.0;

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
                }
            }
        }

        // Create a new map for the reduced parameters
        Map<String, String> reducedParams = new HashMap<>();
        reducedParams.put("nf", Integer.toString(totalNf.get()));
        reducedParams.put("areapd", Double.toString(totalAreapd));
        reducedParams.put("perimpd", Double.toString(totalPerimpd));

        return reducedParams;
    }
}
