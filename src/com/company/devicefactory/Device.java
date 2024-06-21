package com.company.devicefactory;

import com.company.netFactory.Net;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract class representing a generic device.
 */
public abstract class Device {

    // Fields common to all devices
    private int numberOfPins;
    private String deviceType;
    private String name;
    private String modelName;
    private Map<String, String> pinsAndNets = new HashMap<>();
    private Map<String, Net> pinNetMap = new HashMap<>();
    private Map<String, String> params = new HashMap<>();

    /**
     * Constructor for initializing a device with basic information.
     * @param deviceType The type of the device.
     * @param name The name of the device.
     * @param modelName The model name of the device.
     */
    public Device(String deviceType, String name, String modelName) {
        this.deviceType = deviceType;
        this.name = name;
        this.modelName = modelName;

    }

    /**
     * Default constructor.
     */
    public Device() {
        // Default constructor
    }

    /**
     * Extracts the name of the device from the given line of text.
     * @param deviceLine The line containing device information.
     */
    public void extractName(String[] deviceLine) {
        Pattern namePattern = Pattern.compile("[A-Z]+[0-9]+");

        for (String token : deviceLine) {
            if(namePattern.matcher(token).find()) {
                name = token;
                break;
            }
        }
    }

    /**
     * Abstract method to be implemented by subclasses for extracting pins and nets.
     * @param deviceLine The line containing device information.
     */
    public abstract void extractPinsAndNets(String[] deviceLine);

    /**
     * Extracts the model name of the device from the given line of text.
     * @param deviceLine The line containing device information.
     * @param modelName The model name to search for.
     */
    public void extractModel(String[] deviceLine, String modelName) {
        String patternString = "(?<![0-9])" + modelName + "(?![0-9a-z])";
        Pattern namePattern = Pattern.compile(patternString);

        for (String token : deviceLine) {
            if (namePattern.matcher(token).find()) {
                this.modelName = token;
                break;
            }
        }
    }

    /**
     * Extracts parameters of the device from the given line of text.
     * @param deviceLine The line containing device information.
     */
    public void extractParams(String[] deviceLine){
        Pattern paramsPattern = Pattern.compile("[A-Z||a-z]=[0-9]+.*");

        for (String token : deviceLine) {
            if(paramsPattern.matcher(token).find()) {
                String[] paramsKeyValue = token.split("=");
                params.put(paramsKeyValue[0], paramsKeyValue[1]);
            }
        }
    }

    /**
     * Sets pin and net mapping for the device.
     * @param pin The pin to set.
     * @param net The net associated with the pin.
     */
    public void setPinNetMap(String pin, Net net) {
        this.pinNetMap.put(pin, net);
    }

    /**
     * Gets the pin and net mapping of the device.
     * @return The pin and net mapping.
     */
    public Map<String, Net> getPinNetMap() {
        return this.pinNetMap;
    }

    /**
     * Sets the pin and net mapping of the device.
     * @param pinNetMap The pin and net mapping to set.
     */
    public void setPinNetMap(Map<String, Net> pinNetMap) {
        this.pinNetMap = pinNetMap;
    }

    /**
     * Returns a formatted string representation of pins and nets.
     * @return String representation of pins and nets.
     */
    public String getPinsAndNetsStr() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : pinsAndNets.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("-");
        }

        return stringBuilder.toString();
    }

    /**
     * Returns a formatted string representation of pin and net mapping.
     * @return String representation of pin and net mapping.
     */
    public String getPinNetMapString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, Net> entry : pinNetMap.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append("-");
        }

        return stringBuilder.toString();
    }

    /**
     * Gets the name of the device.
     * @return The name of the device.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the device.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the model name of the device.
     * @return The model name of the device.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name of the device.
     * @param modelName The model name to set.
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Gets the pins and nets mapping of the device.
     * @return The pins and nets mapping.
     */
    public Map<String, String> getPinsAndNets() {
        return pinsAndNets;
    }

    /**
     * Sets the pins and nets mapping of the device.
     * @param pinsAndNets The pins and nets mapping to set.
     */
    public void setPinsAndNets(Map<String, String> pinsAndNets) {
        this.pinsAndNets = pinsAndNets;
    }

    /**
     * Gets the device type.
     * @return The device type.
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the device type.
     * @param deviceType The device type to set.
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Gets the parameters of the device.
     * @return The parameters of the device.
     */
    protected Map<String, String> getParams(){
        return this.params;
    }

    /**
     * Sets the parameters of the device.
     * @param params The parameters to set.
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Clears all parameters of the device.
     */
    public void clearParams() {
        this.params.clear();
    }

    public abstract Device reduceParams(Map<String, String> params);
}
