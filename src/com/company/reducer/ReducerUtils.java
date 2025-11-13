package com.company.reducer;

import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.Node;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Contains all shared logic used by both sequential and parallel reducers.
 * Guarantees consistent reduction behavior across both implementations.
 */
public class ReducerUtils {

    /**
     * Returns true if device is eligible for reduction.
     * Pattern ("_ptrn") devices are skipped entirely.
     */
    public static boolean isEligible(Device device) {
        return device != null && !device.getName().endsWith("_ptrn");
    }

    /**
     * Extracts devices from a given net node, ensures they haven't been visited,
     * filters out pattern devices, groups parallel devices, and returns reduced groups.
     */
    public static List<Device> collectFromNetNode(
            Graph topology,
            Node netNode,
            Set<String> visitedDevices)
    {
        List<Connection> connections = topology.adjacencyList.get(netNode);
        if (connections == null) return Collections.emptyList();

        // 1. Collect eligible & not-yet-visited devices
        List<Device> localDevices = connections.stream()
                .map(Connection::getNode)
                .map(Node::getDevice)
                .filter(ReducerUtils::isEligible)
                .filter(device -> visitedDevices.add(device.getName()))
                .collect(Collectors.toList());

        if (localDevices.isEmpty()) return Collections.emptyList();

        // 2. Group by model name → pins/nets
        Map<String, Map<String, List<Device>>> grouped = groupByModelAndPins(localDevices);

        // 3. Combine parallel devices
        return combineGroupedDevices(grouped);
    }

    /**
     * Groups devices by model name and pin-net identity.
     */
    public static Map<String, Map<String, List<Device>>> groupByModelAndPins(List<Device> devices) {
        return devices.stream().collect(
                Collectors.groupingBy(
                        Device::getModelName,
                        Collectors.groupingBy(
                                d -> d.getPinsAndNets().toString(),
                                Collectors.toList()
                        )
                )
        );
    }

    /**
     * Combines grouped devices: each parallel group → 1 combined device.
     */
    public static List<Device> combineGroupedDevices(
            Map<String, Map<String, List<Device>>> grouped)
    {
        List<Device> result = new ArrayList<>();

        for (Map<String, List<Device>> pinGroup : grouped.values()) {
            for (List<Device> group : pinGroup.values()) {
                if (group.size() == 1) {
                    result.add(group.get(0));
                } else {
                    result.add(DeviceFactory.createCombinedDevice(group));
                }
            }
        }

        return result;
    }
}
