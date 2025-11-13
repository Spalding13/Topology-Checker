package com.company.gui;

import com.company.graph.Graph;
import com.company.graph.Node;
import com.company.netFactory.Net;
import com.company.devicefactory.Device;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GraphView with pan and zoom and a more circuit-like rendering:
 * - Devices drawn as rectangles with small pin markers
 * - Nets drawn as horizontal buses; ports appear as left-origin busses
 * - Pin labels displayed near stubs
 * - Mouse drag = pan, mouse wheel = zoom (centered on cursor)
 */
public class GraphView extends Pane {

    private Graph graph;
    private final Group content = new Group();

    private double scale = 1.0;
    private Set<String> portNames = new HashSet<>();
    private List<String> portOrder = new ArrayList<>();
    private String deviceShape = "circle"; // or "square"

    // Pan variables
    private double lastMouseX, lastMouseY;
    private double translateX = 0, translateY = 0;

    public GraphView() {
        setPrefSize(800, 600);
        getChildren().add(content);

        // enable mouse handlers for pan and zoom
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventFilter(ScrollEvent.SCROLL, this::onScroll);
    }

    public void setPorts(List<String> portsInOrder) {
        this.portOrder = portsInOrder == null ? new ArrayList<>() : new ArrayList<>(portsInOrder);
        this.portNames = new HashSet<>(this.portOrder);
        redraw();
    }

    public void setDeviceShape(String shape) {
        if (shape != null && (shape.equalsIgnoreCase("circle") || shape.equalsIgnoreCase("square"))) {
            this.deviceShape = shape.toLowerCase();
            redraw();
        }
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        redraw();
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            translateX += dx;
            translateY += dy;
            content.setTranslateX(translateX);
            content.setTranslateY(translateY);
        }
    }

    private void onScroll(ScrollEvent e) {
        // Zoom centered on mouse pointer
        double oldScale = scale;
        double delta = Math.exp(e.getDeltaY() * 0.001); // smooth zoom
        scale = clamp(scale * delta, 0.1, 6.0);

        // mouse coords relative to content
        double mouseX = (e.getX() - content.getTranslateX()) / oldScale;
        double mouseY = (e.getY() - content.getTranslateY()) / oldScale;

        content.setScaleX(scale);
        content.setScaleY(scale);

        // adjust translation so that the point under the mouse stays under the mouse after scaling
        translateX = e.getX() - mouseX * scale;
        translateY = e.getY() - mouseY * scale;
        content.setTranslateX(translateX);
        content.setTranslateY(translateY);

        e.consume();
    }

    private double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private void redraw() {
        content.getChildren().clear();
        if (graph == null) return;

        // label occupancy rectangles to avoid overlap: list of [x, y, w, h]
        List<double[]> occupied = new ArrayList<>();

        // Layout devices in a grid
        Collection<Node> deviceNodes = graph.deviceNodeMap.values();
        int dCount = deviceNodes.size();
        double leftMargin = 160; // room for port buses
        double rightMargin = 60;
        double topMargin = 40;
        double availableWidth = Math.max(getPrefWidth() - leftMargin - rightMargin, 200);
        double availableHeight = Math.max(getPrefHeight() - 80, 200);

        int columns = Math.max(1, (int) Math.ceil(Math.sqrt(Math.max(1, dCount))));
        int rows = (int) Math.ceil((double) dCount / columns);

        double xGap = columns == 1 ? 0 : (availableWidth / (columns - 1));
        double yGap = availableHeight / (rows + 1);

        List<PlacedDevice> placedDevices = new ArrayList<>();
        Map<String, PlacedDevice> deviceMap = new HashMap<>();

        int i = 0;
        for (Node dn : deviceNodes) {
            Device dev = dn.getDevice();
            int col = i % columns;
            int row = i / columns;
            double x = leftMargin + col * xGap;
            double y = topMargin + (row + 1) * yGap;
            PlacedDevice pd = new PlacedDevice(dn, x, y, i);
            placedDevices.add(pd);
            deviceMap.put(dev.getName(), pd);
            i++;
        }



        // Draw devices (circle or square) with centered label, and pin markers+labels (no mid-stub labels)
        for (PlacedDevice pd : placedDevices) {
            double size = 24;
            javafx.scene.Node devGlyph;
            if ("circle".equals(deviceShape)) {
                Circle devCircle = new Circle(pd.x, pd.y, size/2, Color.web("#ECEFF1"));
                devCircle.setStroke(Color.web("#37474F"));
                devGlyph = devCircle;
            } else {
                Rectangle rect = new Rectangle(pd.x - size/2, pd.y - size/2, size, size);
                rect.setArcWidth(6);
                rect.setArcHeight(6);
                rect.setFill(Color.web("#ECEFF1"));
                rect.setStroke(Color.web("#37474F"));
                devGlyph = rect;
            }
            content.getChildren().add(devGlyph);

            Text label = new Text(pd.x, pd.y + size/2 + 8, pd.node.toString());
            label.setFont(Font.font(11));
            label.setFill(Color.web("#263238"));
            label.setX(pd.x - label.getLayoutBounds().getWidth() / 2);

            // add label with occupancy check to avoid overlap
            double lx = label.getX();
            double ly = label.getY() - label.getLayoutBounds().getHeight();
            double lw = label.getLayoutBounds().getWidth();
            double lh = label.getLayoutBounds().getHeight();
            // simple collision resolve: shift down until free
            while (intersectsAny(occupied, lx, ly, lw, lh)) {
                ly += lh + 2;
            }
            label.setY(ly + lh);
            occupied.add(new double[]{lx, ly, lw, lh});
            content.getChildren().add(label);


            Tooltip.install(devGlyph, new Tooltip("Device: " + pd.node.toString()));
        }

        // Draw ordered port buses on left (avoid label overlap)
        double portSpacing = 36;
        for (int idx = 0; idx < portOrder.size(); idx++) {
            String portName = portOrder.get(idx);
            Node netNode = graph.netNodeMap.get(portName);
            double busStartX = 20;
            double maxX = leftMargin - 20;
            double busY;
            List<PlacedDevice> targets = new ArrayList<>();
            if (netNode != null) {
                Net net = netNode.getNet();
                List<Device> devices = net.getDevicesConnectedToNet();
                if (devices != null) for (Device d : devices) { PlacedDevice pd = deviceMap.get(d.getName()); if (pd != null) targets.add(pd); }
            }
            if (!targets.isEmpty()) {
                double sumY = 0; for (PlacedDevice p : targets) { maxX = Math.max(maxX, p.x); sumY += p.y; } busY = sumY / targets.size();
            } else busY = topMargin + (idx + 1) * portSpacing;

            Line portBus = new Line(busStartX, busY, maxX, busY);
            portBus.setStroke(Color.web("#2196F3"));
            portBus.setStrokeWidth(3.5);
            content.getChildren().add(portBus);

            // add port label with occupancy check
            Text pl = new Text(busStartX + 4, busY - 6, portName);
            pl.setFont(Font.font(11)); pl.setFill(Color.web("#BBDEFB"));
            double plx = pl.getX(); double ply = pl.getY() - pl.getLayoutBounds().getHeight(); double plw = pl.getLayoutBounds().getWidth(); double plh = pl.getLayoutBounds().getHeight();
            while (intersectsAny(occupied, plx, ply, plw, plh)) { ply += plh + 2; pl.setY(ply + plh); }
            occupied.add(new double[]{plx, ply, plw, plh});
            content.getChildren().add(pl);

            // stubs connect from device center
            for (PlacedDevice p : targets) {
                Line stub = new Line(p.x, p.y, p.x, busY);
                stub.setStroke(Color.web("#90A4AE")); stub.setStrokeWidth(1.4);
                content.getChildren().add(stub);
                // draw a small junction dot where the stub meets the bus to indicate a connection
                Circle junction = new Circle(p.x, busY, 3, Color.web("#0D47A1"));
                content.getChildren().add(junction);
            }
        }

        // Draw remaining nets as buses
        Collection<Node> netNodes = graph.netNodeMap.values();
        for (Node netNode : netNodes) {
            String netName = netNode.getNet().getName();
            if (portNames.contains(netName)) continue;
            Net net = netNode.getNet();
            List<Device> devices = net.getDevicesConnectedToNet();
            if (devices == null || devices.isEmpty()) continue;

            List<PlacedDevice> targets = new ArrayList<>();
            for (Device d : devices) { PlacedDevice pd = deviceMap.get(d.getName()); if (pd != null) targets.add(pd); }
            if (targets.isEmpty()) continue;

            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE; double sumY = 0;
            for (PlacedDevice p : targets) { minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x); sumY += p.y; }
            double busY = sumY / targets.size(); if (Math.abs(maxX - minX) < 20) maxX = minX + 20;

            Line bus = new Line(minX, busY, maxX, busY); bus.setStroke(Color.web("#90CAF9")); bus.setStrokeWidth(2.0); content.getChildren().add(bus);

            Text label2 = new Text((minX + maxX) / 2 + 4, busY - 6, netName); label2.setFont(Font.font(10)); label2.setFill(Color.web("#A5D6A7"));
            double l2x = label2.getX(); double l2y = label2.getY() - label2.getLayoutBounds().getHeight(); double l2w = label2.getLayoutBounds().getWidth(); double l2h = label2.getLayoutBounds().getHeight();
            while (intersectsAny(occupied, l2x, l2y, l2w, l2h)) { l2y += l2h + 2; label2.setY(l2y + l2h); }
            occupied.add(new double[]{l2x, l2y, l2w, l2h}); content.getChildren().add(label2);

            for (PlacedDevice p : targets) {
                Line stub = new Line(p.x, p.y, p.x, busY); stub.setStroke(Color.web("#90A4AE")); stub.setStrokeWidth(1.2);
                content.getChildren().add(stub);
                // small junction dot at stub->bus connection
                Circle junction = new Circle(p.x, busY, 3, Color.web("#0D47A1"));
                content.getChildren().add(junction);
            }
        }

        Bounds b = content.getBoundsInParent();
        setPrefWidth(Math.max(getPrefWidth(), b.getWidth() + 40));
        setPrefHeight(Math.max(getPrefHeight(), b.getHeight() + 40));

        // keep current transform
        content.setScaleX(scale);
        content.setScaleY(scale);
        content.setTranslateX(translateX);
        content.setTranslateY(translateY);
    }

    private boolean intersectsAny(List<double[]> occ, double x, double y, double w, double h) {
        for (double[] r : occ) {
            double rx = r[0], ry = r[1], rw = r[2], rh = r[3];
            if (x < rx + rw && x + w > rx && y < ry + rh && y + h > ry) return true;
        }
        return false;
    }

    private static class PlacedDevice {
        Node node;
        double x, y;
        int index;
        PlacedDevice(Node node, double x, double y, int index) { this.node = node; this.x = x; this.y = y; this.index = index; }
    }
}
