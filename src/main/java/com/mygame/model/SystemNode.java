package com.mygame.model;

import com.mygame.engine.TimeController;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SystemNode {
    private double x, y, width, height;
    private List<Port> inputPorts = new ArrayList<>();
    private List<Port> outputPorts = new ArrayList<>();

    public Queue<Packet> getQueuedPackets() {
        return packetQueue;
    }

    private Queue<Packet> packetQueue = new LinkedList<>();
    private final int MAX_QUEUE_SIZE = 5;
    private final int timeSendFromPort = Database.timeSendFromPort; // seconds
    private double timer = 0;


    public SystemNode(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = Database.widthNodes;
        this.height = Database.lengthNodes;
    }
    public void update(double dt, List<Packet> worldPackets) {
        timer += dt;
        // First, update port busy-timers clearly
        for (Port port : outputPorts) {
            port.updateBusyTimer(dt, timeSendFromPort);
        }

        if (timer >= timeSendFromPort) {
            TimeController.setFirstStart(false);
            //timer -= timeSendFromPort;
            timer %= timeSendFromPort;
            // try each port in turn
            for (Port outPort : outputPorts) {
                if (!outPort.isBusy() && outPort.getConnectedPort() != null && !packetQueue.isEmpty()) {
                    Packet packet = packetQueue.poll();
                    // mark it mobile & set its path
                    packet.setMobile(true);
                    packet.setPath(
                            outPort.getCenter(),
                            outPort.getConnectedPort().getCenter()
                    );
                    worldPackets.add(packet);
                    outPort.setBusy(true);
                }
            }

//            if (!packetQueue.isEmpty()) {
//                Packet packet = packetQueue.poll();
//                for (Port outPort : outputPorts) {
//                    if (!outPort.isBusy() && outPort.getConnectedPort()!=null) {
//
//                        packet.setMobile(true);
//                        worldPackets.add(packet);
//                        outPort.setBusy(true);
//                        packet.setPath(outPort.getCenter(), outPort.getConnectedPort().getCenter());
//                        break;
//                    }
//                }
//                //System.out.println(packet.getPosition()+"____"+packet.isMobile()+"____"+worldPackets.contains(packet));
//                assert packet != null;
//                if (!packet.isMobile()) {
//                    packet.setUnAlive();
//                    worldPackets.add(packet);
//                    packet.setMobile(true);
//                }
//
//            }
        }
    }
    public void enqueuePacket(Packet packet) {
        if (packetQueue.size() < MAX_QUEUE_SIZE) {
            packetQueue.offer(packet);
            packet.setMobile(false);
        } else {
            packet.setUnAlive();  // lost
        }
    }

    public void addInputPort(Port.PortType type, Vector2D relativePosition) {
        Vector2D absolutePosition = new Vector2D(x + relativePosition.x, y + relativePosition.y);
        inputPorts.add(new Port(this, type, Port.PortDirection.INPUT, absolutePosition));
    }
    public void addOutputPort(Port.PortType type, Vector2D relativePosition) {
        Vector2D absolutePosition = new Vector2D(getX() + relativePosition.x, getY() + relativePosition.y);
        outputPorts.add(new Port(this, type, Port.PortDirection.OUTPUT, absolutePosition));
    }

    public List<Port> getInputs() { return inputPorts; }
    public List<Port> getOutputs() { return outputPorts; }
    public List<Port> getPorts() {
        List<Port> ports = new ArrayList<>();
        ports.addAll(getInputs());
        ports.addAll(getOutputs());
        return ports;
    }
    public void getPortsPrinted() {
        List<Port> ports = getPorts();
        for (Port port: ports)
            System.out.println(port.getCenter().toString());
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public SystemNode copy() {
        SystemNode clone = new SystemNode(getX(), getY());
        // Copy ports
        for (Port port : getInputs()) {
            clone.addInputPort(port.getType(), port.getPosition());
        }
        for (Port port : getOutputs()) {
            clone.addOutputPort(port.getType(), port.getPosition());
        }

        return clone;
    }

    public Vector2D getPosition() {
        return new Vector2D(getX(), getY());
    }
    public boolean isAllConnected(){
        // Connection status indicator
        boolean allConnected = getPorts().stream()
                .allMatch(port -> port.getConnectedPort() != null);
        return allConnected;

    }
}
