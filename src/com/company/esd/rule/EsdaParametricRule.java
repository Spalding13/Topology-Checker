package com.company.esd.rule;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import java.util.List;
import java.util.Map;

public class EsdaParametricRule implements ParametricRule {

    private static final double MIN_AREA_UM2 = 20.0;
    private static final double MIN_NF = 1;
    private String message = "";


    @Override
    public boolean analyze() {
        System.out.println("\nCommencing EsdaParametric rule...");
        Map<String, List<Device>> deviceByType = DeviceFactory.getDevicesByModel();
        List<Device> esddiodes = deviceByType.getOrDefault("esddiode", List.of());

        boolean allValid = true;
        StringBuilder msgBuilder = new StringBuilder();

        for (Device device : esddiodes) {

            // TODO: In future, exclude mock/pattern devices at factory level instead of filtering here
            if (device.getName().toLowerCase().contains("_ptrn")) {
                continue; // Skip structural-only mock devices
            }

            double area = parseScientificNotation(device.getParam("areapd")) * 1e12; // convert m² → µm²
            System.out.println(device.getName());
            System.out.println(area + " µm²");
            double nf = Double.parseDouble(device.getParam("nf"));

            if (area < MIN_AREA_UM2 || nf < MIN_NF) {
                msgBuilder.append(String.format(
                        "Violation: %s (area=%.4f µm², nf=%.2f)%n",
                        device.getName(), area, nf
                ));
                allValid = false;
            }
        }

        if (!allValid) setMessage(msgBuilder.toString().trim());
        return allValid;
    }

    public static double parseScientificNotation(String value) {
        if (value == null || value.isEmpty()) return 0.0;

        value = value.trim().toLowerCase();

        // Handle engineering suffixes
        if (value.endsWith("f")) return Double.parseDouble(value.replace("f", "")) * 1e-15;
        if (value.endsWith("p")) return Double.parseDouble(value.replace("p", "")) * 1e-12;
        if (value.endsWith("n")) return Double.parseDouble(value.replace("n", "")) * 1e-9;
        if (value.endsWith("u")) return Double.parseDouble(value.replace("u", "")) * 1e-6;
        if (value.endsWith("m")) return Double.parseDouble(value.replace("m", "")) * 1e-3;
        if (value.endsWith("k")) return Double.parseDouble(value.replace("k", "")) * 1e3;
        if (value.endsWith("meg")) return Double.parseDouble(value.replace("meg", "")) * 1e6;
        if (value.endsWith("g")) return Double.parseDouble(value.replace("g", "")) * 1e9;

        // Default: standard scientific notation
        return Double.parseDouble(value);
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
