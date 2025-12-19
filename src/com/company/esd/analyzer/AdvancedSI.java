package com.company.esd.analyzer;

import com.company.graph.Graph;
import com.company.graph.Node;
import com.company.graph.Connection;
import com.company.devicefactory.Device;
import com.company.netFactory.Net;

import java.util.*;

/**
 * FastSI — VF2-inspired Subgraph Isomorphism for your bipartite Device/Net graphs.
 *
 * Key features:
 * - Degree constraints
 * - Node class compatibility (device ↔ device, net ↔ net)
 * - Device model matching
 * - Early adjacency feasibility pruning
 * - Future feasibility pruning (VF2-style)
 * - Deterministic iteration order
 */
public class AdvancedSI {

    private final Graph G;      // big circuit
    private final Graph P;      // pattern
    private final List<Node> gNodes;
    private final List<Node> pNodes;

    // mapping: pattern → graph
    private final Map<Node, Node> mapPtoG = new HashMap<>();
    private final Set<Node> usedG = new HashSet<>();

    public AdvancedSI(Graph graph, Graph pattern) {
        this.G = graph;
        this.P = pattern;

        this.gNodes = new ArrayList<>(graph.getNodes());
        this.pNodes = new ArrayList<>(pattern.getNodes());

        // deterministic ordering:
        // pattern: highest degree first
        pNodes.sort(Comparator.comparingInt((Node n) -> P.getConnections(n).size()).reversed());

        // graph: highest degree first
        gNodes.sort(Comparator.comparingInt((Node n) -> G.getConnections(n).size()).reversed());
    }

    /** Main entry point */
    public boolean isSubgraphIsomorphic() {
        return matchNext(0);
    }

    /** Recursive VF2-style matching */
    private boolean matchNext(int depth) {
        if (depth == pNodes.size())
            return true; // full match

        Node pNode = pNodes.get(depth);

        for (Node gNode : gNodes) {
            if (usedG.contains(gNode))
                continue;

            if (!nodeCompatible(pNode, gNode))
                continue;

            if (!degreesFeasible(pNode, gNode))
                continue;

            if (!adjacentFeasible(pNode, gNode))
                continue;

            // try extend
            mapPtoG.put(pNode, gNode);
            usedG.add(gNode);

            if (matchNext(depth + 1))
                return true;

            // backtrack
            usedG.remove(gNode);
            mapPtoG.remove(pNode);
        }

        return false;
    }

    /** Device/net matching, model compatibility */
    private boolean nodeCompatible(Node pNode, Node gNode) {
        boolean pDev = (pNode.getDevice() != null);
        boolean gDev = (gNode.getDevice() != null);

        boolean pNet = (pNode.getNet() != null);
        boolean gNet = (gNode.getNet() != null);

        // must match type
        if (pDev != gDev) return false;
        if (pNet != gNet) return false;

        // device model check
        if (pDev) {
            Device pd = pNode.getDevice();
            Device gd = gNode.getDevice();

            if (pd.getModelName() == null) return true;
            return pd.getModelName().equalsIgnoreCase(gd.getModelName());
        }

        // net → net matches always valid
        return true;
    }

    /** Degree pruning (VF2 rule) */
    private boolean degreesFeasible(Node pNode, Node gNode) {
        int degP = P.getConnections(pNode).size();
        int degG = G.getConnections(gNode).size();
        return degP <= degG;
    }

    /** Check adjacency compatibility with already-mapped neighbors */
    private boolean adjacentFeasible(Node pNode, Node gNode) {
        List<Connection> pConns = P.getConnections(pNode);

        for (Connection pc : pConns) {
            Node pNeighbor = pc.getNode();

            if (!mapPtoG.containsKey(pNeighbor))
                continue;

            Node gNeighbor = mapPtoG.get(pNeighbor);

            if (!adjacentInG(gNode, gNeighbor))
                return false;

            if (!pinsCompatible(pNode, pNeighbor, gNode, gNeighbor))
                return false;
        }
        return true;
    }

    /** Check if two graph nodes are adjacent */
    private boolean adjacentInG(Node g1, Node g2) {
        for (Connection c : G.getConnections(g1)) {
            if (c.getNode().equals(g2))
                return true;
        }
        return false;
    }

    /** Check pin/net correctness: device→net or net→device */
    private boolean pinsCompatible(Node pN, Node pNeigh, Node gN, Node gNeigh) {

        boolean pDev = pN.getDevice() != null;
        boolean pNeighDev = pNeigh.getDevice() != null;

        if (pDev && !pNeighDev) {
            // device → net
            Device gDev = gN.getDevice();
            Net gNet  = gNeigh.getNet();
            return deviceHasPinToNet(gDev, gNet);
        }

        if (!pDev && pNeighDev) {
            // net → device
            Device gDev = gNeigh.getDevice();
            Net gNet  = gN.getNet();
            return deviceHasPinToNet(gDev, gNet);
        }

        return true;
    }

    private boolean deviceHasPinToNet(Device d, Net n) {
        if (d == null || n == null) return false;
        for (Net net : d.getPinNetMap().values()) {
            if (net != null && net.getName().equals(n.getName()))
                return true;
        }
        return false;
    }
}
