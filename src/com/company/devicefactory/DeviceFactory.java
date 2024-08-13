package com.company.devicefactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class DeviceFactory {

    // Map to store devices grouped by their types
    private static final Map<String, List<Device>> deviceByType = new HashMap<>();

    // Map to store devices by their names for easy lookup
    private static final Map<String, Device> deviceByName = new HashMap<>();

    // Map to associate device type identifiers with their constructors
    private static final Map<String, BiFunction<String, Device, Device>> deviceConstructorMap = new HashMap<>();

    // Static block to initialize maps
    static {
        initializeDeviceByTypeMap();
        initializeDeviceConstructorMap();
    }

    /**
     * Initializes the map to store devices grouped by their types.
     */
    private static void initializeDeviceByTypeMap() {
        deviceByType.put("esddiode", new ArrayList<>());
        deviceByType.put("esdvertpnp", new ArrayList<>());
        // Add more device types if needed
    }

    /**
     * Initializes the map to associate device type identifiers with their constructors.
     */
    private static void initializeDeviceConstructorMap() {
        deviceConstructorMap.put("esddiode", (line, device) -> line != null ? new Esddiode(line) : new Esddiode(device));
        deviceConstructorMap.put("esdvertpnp", (line, device) -> line != null ? new Esdvertpnp(line) : new Esdvertpnp(device));
        // Add more device types and their constructors as needed
    }

    /**
     * Creates a combined device from a list of devices.
     *
     * @param devices    List of devices to be combined.
     * @param deviceType Type of the combined device.
     * @return A combined Device object.
     */
    public static Device createCombinedDevice(List<Device> devices, String deviceType) {
        if (devices == null || devices.isEmpty()) {
            throw new IllegalArgumentException("Device list cannot be null or empty.");
        }

        // Create a new combined device using the first device as a template
        Device combinedDevice = createDeviceFromTemplate(devices.get(0));

        // Set the device type
        combinedDevice.setDeviceType(deviceType);

        // Generate the combined name
        String combinedName = generateCombinedName(devices);
        combinedDevice.setName(combinedName);

        // Set the model name for the combined device (use the model name of the first device)
        combinedDevice.setModelName(devices.get(0).getModelName());

        // Combine the pinsAndNets maps of all devices in the group
        Map<String, String> combinedPinsAndNets = new HashMap<>();
        for (Device device : devices) {
            combinedPinsAndNets.putAll(device.getPinsAndNets());
        }
        combinedDevice.setPinsAndNets(combinedPinsAndNets);

        // Set the pinNetMap for the combined device (use the pinNetMap of the first device)
        combinedDevice.setPinNetMap(devices.get(0).getPinNetMap());

        // Return the combined device
        return combinedDevice;
    }

    /**
     * Generates the combined name for a list of devices.
     *
     * @param devices List of devices.
     * @return Combined name.
     */
    private static String generateCombinedName(List<Device> devices) {
        if (devices.size() == 1) {
            return devices.get(0).getName();
        } else {
            return devices.stream()
                    .map(Device::getName)
                    .reduce((name1, name2) -> name1 + "_" + name2) // Concatenate names with "_"
                    .orElse("");  // Handle empty list case by providing a default value
        }
    }


    /**
     * Creates devices from a list of device lines.
     *
     * @param deviceLines List of device lines.
     * @return List of created devices.
     */
    public static List<Device> createDevicesFromLines(List<String> deviceLines) {
        // Initialize the list to store created devices
        List<Device> devices = new ArrayList<>();

        // Initialize a local map to accumulate devices by type
        Map<String, List<Device>> deviceByTypeLocal = new ConcurrentHashMap<>();

        // Iterate through each device line in parallel
        deviceLines.parallelStream().forEach(line -> {
            // Extract the device type and create the device
            Device device = createDevice(line);

            // Add the device to the corresponding type list in the local map
            deviceByTypeLocal.computeIfAbsent(device.getDeviceType(), k -> new ArrayList<>()).add(device);

            // Add the device to the map of devices by name
            synchronized (DeviceFactory.class) {
                deviceByName.put(device.getName(), device);
            }

            // Add the device to the list of devices
            devices.add(device);
        });

        // Merge the local deviceByType map with the global deviceByType map
        synchronized (DeviceFactory.class) {
            deviceByTypeLocal.forEach((type, list) ->
                    deviceByType.computeIfAbsent(type, k -> new ArrayList<>()).addAll(list)
            );
        }

        // Return the list of created devices
        return devices;
    }

    /**
     * Creates a device based on the device line content.
     *
     * @param line The device line.
     * @return The created Device.
     */
    private static Device createDevice(String line) {

        for (Map.Entry<String, BiFunction<String, Device, Device>> entry : deviceConstructorMap.entrySet()) {
            if (line.contains(entry.getKey())) {
                return entry.getValue().apply(line, null);
            }
        }
        throw new IllegalArgumentException("Unable to determine or create device from line: " + line);
    }

    /**
     * Creates a device based on the device object.
     *
     * @param device The existing device object.
     * @return The new Device.
     */
    private static Device createDeviceFromTemplate(Device device) {
        String deviceModelName = device.getModelName().toLowerCase();
        BiFunction<String, Device, Device> constructor = deviceConstructorMap.get(deviceModelName);
        if (constructor != null) {
            return constructor.apply(null, device); // Pass the device object to the constructor
        }
        throw new IllegalArgumentException("Unsupported device model: " + deviceModelName);
    }

    // Getters for deviceByType and deviceByName
    public Map<String, List<Device>> getDevicesByType() {
        return deviceByType;
    }

    public Map<String, Device> getDevicesByName() {
        return deviceByName;
    }
}
