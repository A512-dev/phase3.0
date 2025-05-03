package com.mygame.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SystemNode {
    private double x, y, width, height;
    private List<Port> inputPorts = new ArrayList<>();
    private List<Port> outputPorts = new ArrayList<>();
    private Queue<Packet> packetQueue = new LinkedList<>();
    private final int MAX_QUEUE_SIZE = 5;
    private final int timeSendFromPort = 3; // seconds
    private double timer = 0;

    public SystemNode(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addInput(Port port) {
        inputPorts.add(new Port(this, type, PortDirection.INPUT));
    }
    public void addOutput(Port port) {
        outputPorts.add(new Port(this, type, PortDirection.OUTPUT));
    }

    public List<Port> getInputs() { return inputs; }
    public List<Port> getOutputs() { return outputs; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public SystemNode copy() {
        SystemNode clone = new SystemNode(getX(), getY(), width, height);
        // Copy ports
        for (Port port : getInputs()) {
            clone.addInput(port.copy(clone));
        }
        for (Port port : getOutputs()) {
            clone.addOutput(port.copy(clone));
        }

        return clone;
    }

}
