package com.company.graph;

import com.company.devicefactory.Device;
import com.company.netFactory.Net;

/**
 * Represents a directional connection between a {@link Node} (which may be a Device or a Net)
 * and a specific pin or terminal.
 *
 * <p>Each {@code Connection} encapsulates the notion of directionality implicitly:
 * the order of association — i.e., which node (Device or Net) owns this Connection —
 * defines the traversal direction in the circuit graph.
 *
 * <p>For example, in a graph representation such as:
 * <pre>
 * XD7 = [{ VDD : b }{ net1 : c }{ PAD : e }]
 * </pre>
 * The device XD7 has directional connections from:
 * - its terminal <b>b</b> to the Net <b>VDD</b>,
 * - its terminal <b>c</b> to the Net <b>net1</b>,
 * - its terminal <b>e</b> to the Net <b>PAD</b>.
 *
 * <p>This means that the connection is inherently directional through its pin mapping:
 * the device terminal name (e.g., <code>e</code>, <code>b</code>) determines how the connection
 * should be interpreted during graph traversal and subgraph matching.
 *
 * <p>During ESD rule analysis, this directionality allows the framework to
 * distinguish between, for instance, a diode anode-to-cathode relationship or a
 * transistor emitter-to-collector relationship, without introducing explicit
 * direction flags or enums.
 *
 * <h3>Future Extension: Meta-Tag Generalization Layer</h3>
 * <p>In the future, a generalization or abstraction layer may be added to
 * interpret symbolic tags attached to terminals, such as:
 * <ul>
 *   <li><code>_gnd</code> — marks a net as a ground reference</li>
 *   <li><code>_io</code> — denotes input/output terminal</li>
 *   <li><code>_pwr</code> — identifies power supply terminals</li>
 * </ul>
 * These tags would allow pattern graphs to remain reusable across designs
 * without relying on hardcoded node names. For now, the directionality encoded
 * in the Connection pin and node association provides sufficient precision for
 * subgraph isomorphism.
 */
public class Connection {

    /** The connected node, which may represent a device or a net in the topology. */
    private Node node;

    /** The pin or terminal name through which this connection is established. */
    public String pin;

    /**
     * Constructs a new Connection between a given {@link Node} and a specific pin.
     *
     * @param node The node (Device or Net) connected through this pin.
     * @param pin  The terminal or pin name used for the connection.
     */
    public Connection(Node node, String pin) {
        this.node = node;
        this.pin = pin;
    }

    /** @return The connected {@link Node}. */
    public Node getNode() {
        return node;
    }

    /** @return The terminal or pin name of this connection. */
    public String getPin() {
        return pin;
    }

    /**
     * Returns a human-readable representation of this connection,
     * showing the target node and the associated pin.
     *
     * @return A string in the format "Node : pin"
     */
    @Override
    public String toString() {
        return this.node.toString() + " : " + this.pin;
    }
}
