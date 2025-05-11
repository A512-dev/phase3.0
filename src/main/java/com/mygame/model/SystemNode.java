package com.mygame.model;

import com.mygame.engine.TimeController;
import com.mygame.util.Database;
import com.mygame.util.Vector2D;

import java.util.*;

public class SystemNode {
    private double x, y, width, height;
    private List<Port> inputPorts = new ArrayList<>();
    private List<Port> outputPorts = new ArrayList<>();

    public Queue<Packet> getQueuedPackets() {
        return packetQueue;
    }

    public void setPacketQueue(Queue<Packet> packetQueue) {
        this.packetQueue = packetQueue;
    }

    private Queue<Packet> packetQueue = new LinkedList<>();
    private final int MAX_QUEUE_SIZE = 5;
    private final int timeSendFromPort = Database.timeSendFromPort; // seconds
    //private double timer = 0;


    public SystemNode(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = Database.widthNodes;
        this.height = Database.lengthNodes;
    }
    public void update(double dt, List<Packet> worldPackets) {
        //System.out.println("jdfvjvjerjegjergjegjergj");
        //timer += dt;
        // First, update port busy-timers clearly
        for (Port port : outputPorts) {
            port.updateBusyTimer(dt, timeSendFromPort);
        }
        // 2) Try to send *as many* queued packets as there are free ports
        Iterator<Packet> it = packetQueue.iterator();
        while (it.hasNext()) {
            Packet pkt = it.next();
            for (Port out : outputPorts) {
//                System.out.println("index:"+outputPorts.indexOf(out));
//                System.out.println("busy:"+out.isBusy());
//                System.out.println("connected:"+out.getConnectedPort());
                if (!out.isBusy() && out.getConnectedPort() != null) {
                    // a) mark packet mobile and set its new path
                    pkt.setMobile(true);
                    pkt.position = out.getCenter();
                    pkt.setPath(
                            out.getCenter(),
                            out.getConnectedPort().getCenter()
                    );
                    // b) enqueue it into the world
                    worldPackets.add(pkt);
                    // c) put THIS port on cooldown for 3 s
                    out.setBusy();
                    // d) remove from node queue
                    it.remove();
                    break;  // move on to next queued packet
                }
            }
        }
//        // try each port in turn
//        for (Port outPort : outputPorts) {
//            if (!outPort.isBusy() && outPort.getConnectedPort() != null && !packetQueue.isEmpty()) {
//                Packet packet = packetQueue.poll();
//                // mark it mobile & set its path
//                worldPackets.add(packet);
//                packet.setMobile(true);
//                packet.position = outPort.getCenter();
//                packet.setPath(
//                        outPort.getCenter(),
//                        outPort.getConnectedPort().getCenter()
//                );
//                outPort.setBusy();
//            }
//        }

//        if (timer >= timeSendFromPort) {
//            TimeController.setFirstStart(false);
//            //timer -= timeSendFromPort;
//            timer -= timeSendFromPort;


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
            clone.addInputPort(port.getType(), port.getPosition().subtracted(new Vector2D(x, y)));
        }
        for (Port port : getOutputs()) {
            clone.addOutputPort(port.getType(), port.getPosition().subtracted(new Vector2D(x, y)));
        }
        // DEEP-COPY the queued packets
        Queue<Packet> newQueue = new LinkedList<>();
        for (Packet p : getQueuedPackets()) {
            Packet pCopy = p.copy();
            pCopy.setMobile(false);          // preserve “queued” state
            // you may also need to preserve whatever pathStart/pathEnd they had
            newQueue.offer(pCopy);
        }
        clone.setPacketQueue(newQueue);


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
//    public void emitQueued(List<Packet> worldPackets) {
//        // but ignore the timer – just fire all possible ports once:
//        for (Port outPort : getOutputs()) {
//            if (!outPort.isBusy()
//                    && outPort.getConnectedPort() != null
//                    && !getQueuedPackets().isEmpty()) {
//                Packet p = getQueuedPackets().poll();
//                p.setMobile(true);
//                p.position = outPort.getCenter();
//                p.setPath(
//                        outPort.getCenter(),
//                        outPort.getConnectedPort().getCenter()
//                );
//                worldPackets.add(p);
//                outPort.setBusy();
//            }
//        }
//    }
}
