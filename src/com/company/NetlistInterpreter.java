package com.company;

import java.util.*;
import java.util.regex.Pattern;

public class NetlistInterpreter {

    // Accept devices that start with X, D, M, R, C, Q, etc.
    // This is safe AND avoids false positives.
    private static final Pattern DEVICE_LINE_PT =
            Pattern.compile("^[XDMRCLQ][A-Za-z0-9_]*\\s+.+");

    private static final Pattern PARAM_PT = Pattern.compile("[A-Za-z0-9_]+=.+");

    private final Map<String, List<String>> netlistInfo = new HashMap<>();
    private final List<String> designDetails = new ArrayList<>();
    private final List<String> devices = new ArrayList<>();
    private final Set<String> nets = new HashSet<>();

    public Map<String, List<String>> parseNetlist(String netlist) {

        // Split and normalize CRLF → LF
        List<String> rawLines = Arrays.asList(netlist.replace("\r", "").split("\n"));

        // Merge continuation lines starting with "+"
        List<String> logical = mergeContinuationLines(rawLines);

        for (String line : logical) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Comment → metadata
            if (line.startsWith("*")) {
                parseDesignMetadata(line);
                continue;
            }

            // Skip control cards except .SUBCKT header (we extract ports from it)
            if (line.startsWith(".")) {
                continue;
            }

            // Device line?
            if (DEVICE_LINE_PT.matcher(line).matches()) {
                parseDeviceLine(line);
            }
        }

        netlistInfo.put("designDetails", designDetails);
        netlistInfo.put("devices", devices);
        netlistInfo.put("nets", new ArrayList<>(nets));

        return netlistInfo;
    }

    // ------------------------------------------------------------
    // Merge '+' continuation lines with previous
    // ------------------------------------------------------------
    private List<String> mergeContinuationLines(List<String> raw) {
        List<String> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        for (String line : raw) {
            String t = line.trim();
            if (t.isEmpty()) continue;

            if (t.startsWith("+")) {
                buf.append(" ").append(t.substring(1).trim());
            } else {
                if (buf.length() > 0) out.add(buf.toString());
                buf = new StringBuilder(line);
            }
        }
        if (buf.length() > 0) out.add(buf.toString());
        return out;
    }

    // ------------------------------------------------------------
    // Extract design metadata from comments
    // ------------------------------------------------------------
    private void parseDesignMetadata(String line) {
        String clean = line.replaceFirst("^\\*", "").trim();
        if (clean.contains("Library Name") || clean.contains("Cell Name") || clean.contains("View Name")) {
            designDetails.add(clean);
        }
    }

    // ------------------------------------------------------------
    // Parse device line → preserved exactly for DeviceFactory
    // ------------------------------------------------------------
    /**
     * Core logic to separate Name, Nets, Model, and Params
     */
    private void parseDeviceLine(String line) {
        // Split by ANY whitespace (spaces, tabs, multiple spaces)
        String[] tokens = line.split("\\s+");

        if (tokens.length < 2) return; // Invalid line

        String deviceName = tokens[0];

        // Reconstruct the standardized line for the DeviceFactory
        StringBuilder standardizedLine = new StringBuilder(deviceName);

        boolean parsingNets = true;

        // Iterate from token 1 (after name)
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];

            // 1. Check if we hit a parameter (e.g. w=10u)
            if (token.contains("=")) {
                parsingNets = false;
            }

            // 2. Add to net list if we are still in parsing mode
            if (parsingNets) {
                nets.add(token);
            }

            // 3. Special Handling for 'sub!':
            // 'sub!' is valid net, but it usually marks the end of the net list
            // We perform this check AFTER adding it to the list.
            if (token.equalsIgnoreCase("sub!")) {
                parsingNets = false;
            }

            standardizedLine.append(" ").append(token);
        }

        devices.add(standardizedLine.toString());
    }
}
