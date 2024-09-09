package com.company;

import java.util.*;
import java.util.regex.Pattern;

public class StateMachine {

    // Design info section:
    private Pattern startPt = Pattern.compile(".*auCdl Netlist:.*");
    private Pattern designLinePt = Pattern.compile("\\*[A-Z].* *");

    // Devices section:
    private Pattern devicePt = Pattern.compile("^[A-Z]+[0-9]+");
    private Pattern deviceParamsPt = Pattern.compile("^\\+.*");

    // Output
    private Map<String, List<String>> netlistInfo = new HashMap<>();

    private List<String> designDetails = new ArrayList<>();
    private List<String> devices = new ArrayList<>();
    private List<String> nets = new ArrayList<>();

    // Constructor
    public StateMachine() {
        // Initialize patterns or any other setup if needed
    }

    // Main parsing method
    public Map<String, List<String>> parseNetlist(String netlist) {
        String[] lines = netlist.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (startPt.matcher(lines[i]).find()) {
                i = parseDesignDetails(lines, i + 2);
            } else if (devicePt.matcher(lines[i]).find()) {
                i = parseDeviceParams(lines, i);
            }
        }

        netlistInfo.put("designDetails", designDetails);
        netlistInfo.put("devices", devices);
        netlistInfo.put("nets", nets);

        return netlistInfo;
    }

    private int parseDeviceParams(String[] lines, int i) {
        int currentLineIdx = i;
        String deviceLine = "";

        while (currentLineIdx < lines.length && devicePt.matcher(lines[currentLineIdx]).find()) {
            parseNets(lines, currentLineIdx);

            deviceLine = lines[currentLineIdx]
                    .replaceAll("\\r", "")
                    .replaceAll(" $", "");

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

    private void parseNets(String[] lines, int i) {
        List<String> deviceModels = Arrays.asList("esddiode", "esdvertpnp");
        String[] currentLine = lines[i].split(" ");

        for (int j = 1; j < currentLine.length; j++) {
            String token = currentLine[j];
            if (deviceModels.contains(token)) {
                break;
            } else {
                if (!nets.contains(token)) {
                    nets.add(token);
                }
            }
        }
    }

    private int parseDesignDetails(String[] lines, int i) {
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
