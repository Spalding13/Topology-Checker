package com.company.esd.analyzer;

import com.company.graph.Connection;
import com.company.graph.Graph;
import com.company.graph.Node;
import com.company.netFactory.Net;
import com.company.devicefactory.Device;

import java.util.*;

/**
 * Backtracking subgraph matcher that enforces:
 * - node type (device/net)
 * - device model compatibility
 * - pin -> net consistency (so device orientation is preserved if pattern encodes that)
 * - optional anchoring to a set of port net names
 */
public class NaiveSI {

    private final Graph graph;
    private final Graph pattern;
    private final List<Node> patternNodes;        // ordered for matching (heuristic)
    private final List<Node> graphNodes;
    private final Set<String> ports;              // optional port anchoring (net names)

    // constructors
    public NaiveSI(Graph graph, Graph pattern) {
        this(graph, pattern, null);
    }

    public NaiveSI(Graph graph, Graph pattern, Collection<String> ports) {
        this.graph = graph;
        this.pattern = pattern;
        this.patternNodes = new ArrayList<>(pattern.getNodes());
        this.graphNodes = new ArrayList<>(graph.getNodes());
        this.ports = ports == null ? null : new HashSet<>(ports);

        // heuristic: match most-constrained pattern nodes first (degree descending)
        patternNodes.sort((a, b) -> Integer.compare(pattern.getConnections(b).size(), pattern.getConnections(a).size()));
    }

    public boolean isSubgraphIsomorphic() {
        Map<Node, Node> mapping = new HashMap<>();
        Set<Node> used = new HashSet<>();
        return matchRec(0, mapping, used);
    }

    // recursive backtracking
    private boolean matchRec(int idx, Map<Node, Node> mapping, Set<Node> used) {
        if (idx >= patternNodes.size()) {
            return true; // all pattern nodes mapped consistently
        }

        Node pNode = patternNodes.get(idx);

        for (Node gNode : graphNodes) {
            if (used.contains(gNode)) continue;
            if (!nodeCompatible(pNode, gNode)) continue;

            // port anchoring: if pNode is a net and pattern net name is in ports set,
            // require the graph net's name is also in ports
            if (!portConstraintSatisfied(pNode, gNode)) continue;

            // tentatively map
            mapping.put(pNode, gNode);
            used.add(gNode);

            // check local consistency against already-mapped neighbors
            if (consistentSoFar(mapping, pNode)) {
                if (matchRec(idx + 1, mapping, used)) return true;
            }

            // backtrack
            mapping.remove(pNode);
            used.remove(gNode);
        }
        return false;
    }

    private boolean nodeCompatible(Node pNode, Node gNode) {
        // net ↔ net
        if (pNode.getNet() != null) {
            return gNode.getNet() != null;
        }
        // device ↔ device + model compatibility
        if (pNode.getDevice() != null) {
            if (gNode.getDevice() == null) return false;
            String pModel = safeLower(pNode.getDevice().getModelName());
            String gModel = safeLower(gNode.getDevice().getModelName());
            // allow null/empty model to match any, otherwise exact match or aliasing rules could be added
            if (pModel == null || pModel.isEmpty()) return true;
            return pModel.equals(gModel);
        }
        // fallback: types must match
        return false;
    }

    private boolean portConstraintSatisfied(Node pNode, Node gNode) {
        if (ports == null) return true;
        if (pNode.getNet() != null) {
            String pNetName = pNode.getNet().getName();
            // If pattern net name is one of ports, we require mapped graph net name be one of ports.
            if (ports.contains(pNetName)) {
                return ports.contains(gNode.getNet().getName());
            }
        }
        return true;
    }

    // check edges between the newly-mapped pattern node and any already-mapped neighbors
    private boolean consistentSoFar(Map<Node, Node> mapping, Node pNode) {
        Node gNode = mapping.get(pNode);
        List<Connection> pConns = pattern.getConnections(pNode);
        List<Connection> gConns = graph.getConnections(gNode);

        for (Connection pConn : pConns) {
            Node pNeighbor = pConn.getNode();
            if (!mapping.containsKey(pNeighbor)) continue; // neighbor not mapped yet, can't check

            Node gNeighbor = mapping.get(pNeighbor);

            // there must exist at least one connection in graph between gNode and gNeighbor
            // that is compatible with the pattern connection (pin->net semantics)
            boolean foundMatch = false;
            for (Connection gConn : gConns) {
                if (!gConn.getNode().equals(gNeighbor)) continue;
                if (connectionCompatible(pNode, pConn, gNode, gConn, mapping)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }
        return true;
    }

    /**
     * Checks whether a specific pattern connection is compatible with a candidate graph connection,
     * given the current partial mapping (to interpret pattern neighbor nets correctly).
     */
    private boolean connectionCompatible(Node pNode, Connection pConn, Node gNode, Connection gConn, Map<Node, Node> mapping) {
        // If the pattern side is a device connected to a net neighbor
        if (pNode.getDevice() != null && pConn.getNode().getNet() != null) {
            Device pDevice = pNode.getDevice();
            Node pNetNode = pConn.getNode();
            Node mappedGraphNetNode = mapping.get(pNetNode);
            if (mappedGraphNetNode == null || mappedGraphNetNode.getNet() == null) return false;

            // Check that the graph device (gNode.getDevice()) has a pin that connects to mappedGraphNetNode.getNet()
            Device gDevice = gNode.getDevice();
            return deviceHasPinConnectedToNet(gDevice, mappedGraphNetNode.getNet());
        }

        // If the pattern side is a net connected to a device neighbor
        if (pNode.getNet() != null && pConn.getNode().getDevice() != null) {
            Node pDeviceNode = pConn.getNode();
            Node mappedGraphDeviceNode = mapping.get(pDeviceNode);
            if (mappedGraphDeviceNode == null || mappedGraphDeviceNode.getDevice() == null) return false;

            Device gDevice = mappedGraphDeviceNode.getDevice();
            Net mappedGraphNet = mapping.get(pNode).getNet();
            return deviceHasPinConnectedToNet(gDevice, mappedGraphNet);
        }

        // Other cases are unexpected in bipartite device/net graph
        return false;
    }

    // helper: does device have a pin whose connected net equals targetNet (compare by name)
    private boolean deviceHasPinConnectedToNet(Device device, Net targetNet) {
        if (device == null || targetNet == null) return false;
        Map<String, Net> pinMap = device.getPinNetMap();
        for (Net n : pinMap.values()) {
            if (n != null && n.getName().equals(targetNet.getName())) return true;
        }
        return false;
    }

    private String safeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }
}
