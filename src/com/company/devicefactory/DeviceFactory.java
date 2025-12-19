package com.company.devicefactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeviceFactory dynamically creates Device objects from CDL lines
 * and manages them by both type (e.g., D, Q) and model name (e.g., esddiode).
 * It also supports combining devices for the reduction process.
 */
public class DeviceFactory {

    // Grouped by high-level device type (e.g., D, Q)
    private static final Map<String, List<Device>> devicesByType = new ConcurrentHashMap<>();

    // Grouped by model name (e.g., esddiode, esdvertpnp)
    private static final Map<String, List<Device>> devicesByModel = new ConcurrentHashMap<>();

    // For quick lookup by device name
    private static final Map<String, Device> devicesByName = new ConcurrentHashMap<>();

    private static final Map<String, String> combinedDeviceTrace = new ConcurrentHashMap<>();


    private DeviceFactory() {
        // Prevent instantiation
    }

    /**
     * Creates all devices from CDL lines.
     */
    public static List<Device> createDevicesFromLines(List<String> deviceLines) {
        List<Device> devices = new ArrayList<>();

        for (String line : deviceLines) {
            Device device = createDeviceFromLine(line);
            if (device != null) {

                devices.add(device);

                // Group by high-level type (D, Q, etc.)
                devicesByType
                        .computeIfAbsent(device.getDeviceType(), k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(device);

                // Group by model name (esddiode, esdvertpnp, etc.)
                devicesByModel
                        .computeIfAbsent(device.getModelName().toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(device);

                // Add to name-based lookup
                devicesByName.put(device.getName(), device);
            }
        }

        return devices;
    }

    /**
     * Dynamically creates a Device from its CDL line and model name.
     */
    private static Device createDeviceFromLine(String line) {
        String model = extractModelFromLine(line);

        if (model == null) {
            //System.err.println("[DeviceFactory] ⚠ Could not extract model name from line: " + line);
            return new GenericDevice(line, "unknown");
        }

        String className = "com.company.devicefactory." + capitalize(model);

        try {
            Class<?> clazz = Class.forName(className);
            return (Device) clazz.getConstructor(String.class).newInstance(line);
        } catch (ClassNotFoundException e) {
            System.err.println("[DeviceFactory] ⚠ Unknown model '" + model + "'. Using GenericDevice fallback.");
            return new GenericDevice(line, model);
        } catch (Exception e) {
            throw new RuntimeException("[DeviceFactory] Failed to instantiate device for model: " + model, e);
        }
    }

    public static Device createCombinedDevice(List<Device> devices) {
        if (devices == null || devices.isEmpty()) {
            throw new IllegalArgumentException("Device list cannot be null or empty.");
        }

        Device template = devices.get(0);
        Device combined;

        // Use reflection to duplicate a device of the same type
        try {
            combined = template.getClass().getConstructor(Device.class).newInstance(template);
        } catch (Exception e) {
            System.err.println("[DeviceFactory] ⚠ Could not clone " + template.getDeviceType() + ", using GenericDevice.");
            combined = new GenericDevice(template);
        }

        // Assign combined properties
        combined.setName(generateCombinedName(devices));
        combined.setParams(template.recalculateParallelParams(extractParams(devices)));

        // Merge nets
        Map<String, String> mergedPins = new HashMap<>();
        for (Device d : devices) {
            mergedPins.putAll(d.getPinsAndNets());
        }
        combined.setPinsAndNets(mergedPins);

        // IMPORTANT: Do NOT register the combined device here.
        // Just replace leaf devices with the canonical combined one in the global maps.
        replaceWithCombinedDevice(combined, devices);

        return combined;
    }

    // --- Utility Methods ---
    private static void replaceWithCombinedDevice(Device combined, List<Device> originalDevices) {
        // 1) Remove original (leaf) devices from all global maps
        for (Device d : originalDevices) {
            // Remove from devicesByType
            List<Device> typeList = devicesByType.get(d.getDeviceType());
            if (typeList != null) {
                typeList.remove(d);
            }

            // Remove from devicesByModel
            String modelKey = d.getModelName() != null ? d.getModelName().toLowerCase() : null;
            if (modelKey != null) {
                List<Device> modelList = devicesByModel.get(modelKey);
                if (modelList != null) {
                    modelList.remove(d);
                }
            }

            // Remove from devicesByName
            devicesByName.remove(d.getName());

            // Optional: trace leaf → combined
            combinedDeviceTrace.put(d.getName(), combined.getName());
        }

        // 2) Add combined device once to the canonical maps
        devicesByType
                .computeIfAbsent(combined.getDeviceType(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(combined);

        String combinedModelKey = combined.getModelName() != null ? combined.getModelName().toLowerCase() : null;
        if (combinedModelKey != null) {
            devicesByModel
                    .computeIfAbsent(combinedModelKey, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(combined);
        }

        devicesByName.put(combined.getName(), combined);
    }

    private static List<Map<String, String>> extractParams(List<Device> devices) {
        List<Map<String, String>> params = new ArrayList<>();
        for (Device d : devices) {
            params.add(d.getParams());
        }
        return params;
    }

    private static String generateCombinedName(List<Device> devices) {
        return devices.stream()
                .map(Device::getName)
                .sorted()
                .reduce((a, b) -> a + "_" + b)
                .orElse("COMBINED");
    }

    private static String extractModelFromLine(String line) {
        String[] tokens = line.trim().split("\\s+");
        String lastTokenBeforeParam = null;
        for (String token : tokens) {
            if (token.contains("=")) break;
            lastTokenBeforeParam = token;
        }
        return lastTokenBeforeParam != null ? lastTokenBeforeParam.toLowerCase() : null;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    // --- Getters ---

    public static Map<String, List<Device>> getDevicesByType() {
        return devicesByType;
    }

    public static Map<String, List<Device>> getDevicesByModel() {
        return devicesByModel;
    }

    public static Map<String, Device> getDevicesByName() {
        return devicesByName;
    }

    public static void clear() {
        devicesByType.clear();
        devicesByModel.clear();
        devicesByName.clear();
    }
}
