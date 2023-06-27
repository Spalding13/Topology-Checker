package com.company.devicefactory;

import com.company.netFactory.Net;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class Device {

    public int numberOfPins;

    public String deviceType;

    public String name;
    public String modelName;

    public Map<String, String> pinsAndNets = new HashMap<>();
    public Map<String, Net> pinNetMap = new HashMap<>();
    public Map<String, String> params = new HashMap<>();

    public void extractName(String[] deviceLine) {
        Pattern namePattern = Pattern.compile("[A-Z]+[0-9]+");

        for (String token : deviceLine) {
            if(namePattern.matcher(token).find()) {
                name = token;
                break;
            }
        }
    }

    //public abstract Device reduceParams(Map<String, String> params);

    public abstract void extractPinsAndNets(String[] deviceLine);

    public void extractModel(String[] deviceLine) {
        Pattern namePattern = Pattern.compile("(?<![0-9])[a-z]+(?!=)(?![0-9])(?![a-z]+)");

        for (String token : deviceLine) {
            if(namePattern.matcher(token).find()) {
                modelName = token;
                break;
            }
        }

   }

    public void extractParams(String[] deviceLine){
        Pattern paramsPattern = Pattern.compile("[A-Z||a-z]=[0-9]+.*");

        for (String token : deviceLine) {
            if(paramsPattern.matcher(token).find()) {
                String[] paramsKeyValue = token.split("=");
                params.put(paramsKeyValue[0], paramsKeyValue[1]);
            }
        }
    }

    public void setPinNetMap (String pin, Net net) {
        this.pinNetMap.put(pin, net);
    }

    public String getPinNetMapString () {

        StringBuilder stringBuilder = new StringBuilder();

      for (Map.Entry<String,String> set : this.pinsAndNets.entrySet()) {

          stringBuilder.append(set.getKey() + ":" + set.getValue() + "-");

          System.out.println(set.getKey() + "-" + set.getValue());
      }

      return stringBuilder.toString();

    }



}
