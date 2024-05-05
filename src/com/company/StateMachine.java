package com.company;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

public class StateMachine {

    // Design info section:
    private static Pattern startPt = Pattern.compile(".*auCdl Netlist:.*");
    private static Pattern designLinePt = Pattern.compile("\\*[A-Z].* *");

    // Devices section:
    private static Pattern devicePt = Pattern.compile("^[A-Z]+[0-9]+");
    private static Pattern deviceParamsPt = Pattern.compile("^\\+.*");

    // Input
    private static String[] lines;

    // Output
    private static Map< String, List<String> > netlistInfo = new HashMap<>();

    private static List<String> designDetails = new ArrayList<>();
    private static List<String> devices = new ArrayList<>();
    private static List<String> nets = new ArrayList<>();

    public static Map< String, List<String> > parseNetlist(String netlist) {

        lines = netlist.split("\n");

        for (int i = 0; i < lines.length; i++) {

            if (startPt.matcher(lines[i]).find()) {

                i = parseDesignDetails(i + 2);

            } else if (devicePt.matcher(lines[i]).find()) {
                //parseNets(i);
                i = parseDeviceParams(i);

            }
        }

        netlistInfo.put("designDetails", designDetails);
        netlistInfo.put("devices", devices);
        netlistInfo.put("nets", nets);

        return netlistInfo;
    }


    private static int parseDeviceParams(int i) {


        int currentLineIdx = i;

        String deviceLine = "";



        while (currentLineIdx < lines.length && devicePt.matcher(lines[currentLineIdx]).find()) {
//            System.out.println("DEVICE LINE - > " + deviceLine);

            parseNets(currentLineIdx);

            deviceLine = lines[currentLineIdx]
                    .replaceAll("\\r", "")
                    .replaceAll(" $", "");

            // Add parameters on next line until we encounter another device
            while (currentLineIdx + 1 < lines.length && deviceParamsPt.matcher(lines[currentLineIdx + 1]).find()) {
                deviceLine = deviceLine.concat(lines[currentLineIdx + 1]
                        .replaceAll("^\\+", "")
                        .replaceAll("\\r", ""));
                currentLineIdx++;


            }
            devices.add(deviceLine);
            currentLineIdx++;
        }
        return currentLineIdx;
    }

    private static void parseNets(int i) {

        // We always assume that all nets will be on the same line for every device
        List<String> deviceModels = Arrays.asList("esddiode", "esdvertpnp");

        String[] currentLine = lines[i].split(" ");

        for(int j = 1; j < currentLine.length; j++ ){
            String token = currentLine[j];
            // Make sure we don't add anything other than nets
            if(deviceModels.contains(token)) {
                // If we encounter a model, then we added all nets

                break;
            } else {
                if(!nets.contains(token)){
                    nets.add(token);
                }
            }
        }
    }

    private static int parseDesignDetails(int i) {

        String currentDesignDetail;

        while (i < lines.length && designLinePt.matcher(lines[i]).find()) {

            currentDesignDetail = lines[i]
                    .replaceAll("\\r", "")
                    .replaceAll("^\\* ", "")
                    .replaceAll(": {1,}", "=");

            designDetails.add(currentDesignDetail);
            i++;
        }

        return i;
    }
}
