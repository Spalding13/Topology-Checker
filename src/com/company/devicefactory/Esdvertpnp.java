package com.company.devicefactory;

import java.util.Map;

public class Esdvertpnp extends Device {

    public Esdvertpnp(String deviceLine){
        String[] deviceTokens = deviceLine.split(" ");

        this.numberOfPins = 3;
        this.deviceType = "Q";

        this.extractName(deviceTokens);
        this.extractModel(deviceTokens);
        this.extractPinsAndNets(deviceTokens);
        this.extractParams(deviceTokens);
    }

    @Override
    public void extractPinsAndNets(String[] deviceLine) {
        this.pinsAndNets.put("e", deviceLine[1]);
        this.pinsAndNets.put("b", deviceLine[2]);
        this.pinsAndNets.put("c", deviceLine[3]);

    }


//    @Override
//    public Esddiode reduceParams(Map<String, String> params) {
//        return null;
//    }
}
