package com.company.esd.rule;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import java.util.List;
import java.util.Map;

public class EsdaParametricRule implements ParametricRule {

    private static final double MIN_AREA = 2.0;
    private static final double MIN_NF = 1.0;
    private String message = "";

    @Override
    public boolean analyze() {
        Map<String, List<Device>> deviceByType = DeviceFactory.getDevicesByType();
        List<Device> esddiodes = deviceByType.getOrDefault("esddiode", List.of());

        boolean allValid = true;
        StringBuilder msgBuilder = new StringBuilder();

        for (Device device : esddiodes) {
            double area = Double.parseDouble(device.getParam("area"));
            double nf = Double.parseDouble(device.getParam("nf"));

            if (area < MIN_AREA || nf < MIN_NF) {
                msgBuilder.append(String.format(
                        "Violation: %s (area=%.2f, nf=%.2f)%n",
                        device.getName(), area, nf
                ));
                allValid = false;
            }
        }

        // ✅ If any violations found, set message
        if (!allValid) {
            setMessage(msgBuilder.toString().trim());
        }

        return allValid;
    }

    @Override
    public String getDescription() {
        return "Ensures all ESD diodes meet minimum area and nf thresholds.";
    }

    @Override
    public String getName() {
        return "ESDA_PARAM_AREA_NF";
    }

    @Override
    public String getMessage() {
        return message;
    }

    // Only the parametric rule should be responsible for setting its message
    private void setMessage(String message) {
        this.message = message;
    }
}
