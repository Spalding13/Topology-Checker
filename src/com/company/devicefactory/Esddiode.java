package com.company.devicefactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Esddiode extends Device {

    public Esddiode(String deviceLine){
        String[] deviceTokens = deviceLine.split(" ");
        this.numberOfPins = 2;
        this.deviceType = "D";

        this.extractName(deviceTokens);
        this.extractModel(deviceTokens);
        this.extractPinsAndNets(deviceTokens);
        this.extractParams(deviceTokens);
    }

    @Override
    public void extractPinsAndNets(String[] deviceLine) {
        this.pinsAndNets.put("pos", deviceLine[1]);
        this.pinsAndNets.put("neg", deviceLine[2]);
    }


}
