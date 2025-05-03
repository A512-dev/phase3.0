package com.mygame.model;

import java.util.ArrayList;
import java.util.List;

public class SystemNode {
    private double x, y, width, height;
    private final List<Port> inputs = new ArrayList<>();
    private final List<Port> outputs = new ArrayList<>();

    public SystemNode(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void addInput(Port port) { inputs.add(port); }
    public void addOutput(Port port) { outputs.add(port); }

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
