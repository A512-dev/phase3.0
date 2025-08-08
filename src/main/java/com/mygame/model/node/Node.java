package com.mygame.model.node;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.world.World;
import com.mygame.model.PacketEventListener;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;
import com.mygame.snapshot.NodeSnapshot;

import javax.sound.midi.Soundbank;
import java.util.*;

/** Core data + API every network node shares. */
public abstract class Node implements PacketEventListener {

    /* ── geometry ─────────────────────────────────────────── */
    protected final Vector2D position;     // top-left corner
    protected final double      width;
    protected final double      height;

    /* ── ports ─────────────────────────────────────────────── */
    protected final List<Port> inputs  = new ArrayList<>();
    protected final List<Port> outputs = new ArrayList<>();

    /* ── packet queue (FIFO) ───────────────────────────────── */
    protected final Deque<Packet> queue = new ArrayDeque<>();
    /** --- new field --- */
    protected PacketEventListener packetEventListener;


    /* ── ctor ──────────────────────────────────────────────── */
    protected Node(double x, double y, double width, double height) {
        this.position = new Vector2D(x, y);
        this.width    = width;
        this.height   = height;
    }

    /* ── geometry helpers ─────────────────────────────────── */
    public double getX() { return position.x(); }
    public double getY() { return position.y(); }
    public Vector2D getPosition() { return position; }

    public double    getWidth()  { return width;  }
    public double    getHeight() { return height; }
    public Vector2D getCenter() {
        return new Vector2D(getX() + width/2.0, getY() + height/2.0);
    }

    /* ── port API ─────────────────────────────────────────── */
    public List<Port> getInputs()  { return inputs;  }
    public List<Port> getOutputs() { return outputs; }

    /** --- new setter --- */
    public void setPacketEventListener(PacketEventListener listener) {
        this.packetEventListener = listener;
    }



    public void addInputPort(Port.PortType t, Vector2D relPos){
        Vector2D abs = new Vector2D(position.x() + relPos.x(),
                position.y() + relPos.y());
        Port p = new Port(this, t, abs, Port.PortDirection.INPUT);
        inputs.add(p);
    }
    public void addOutputPort(Port.PortType t, Vector2D relPos){
        Vector2D abs = new Vector2D(position.x() + relPos.x(),
                position.y() + relPos.y());
        Port p = new Port(this, t, abs, Port.PortDirection.OUTPUT);
        outputs.add(p);
    }

    /* ── queue helpers ────────────────────────────────────── */
    public void enqueuePacket(Packet p){ queue.addLast(p); }
    protected void emitQueued(List<Packet> worldPackets){
        if(!queue.isEmpty()){
            Packet p = queue.removeFirst();
            p.setMobile(true);
            worldPackets.add(p);
            System.out.println("Packet Added to World");
        }
    }


    /* ── hooks every concrete node must implement ─────────── */
    /** Called exactly once when packet *enters* this node. */
    public abstract void onDelivered(Packet p);
    public abstract void onDelivered(Packet p, Port port);
    public abstract void onLost(Packet p);



    /** Called each frame. Subclass usually calls emitQueued(). */
    public abstract void update(double dt, List<Packet> worldPackets);

    public List<Port> getPorts() {
        List<Port> allPorts = new ArrayList<>();
        allPorts.addAll(getInputs());
        allPorts.addAll(getOutputs());
        return allPorts;
    }
    /** True if every output has a connection. */
    public boolean isAllConnected() {
        return outputs.stream().allMatch(p -> p.getConnectedPort() != null);
    }


    public Collection<Packet> getQueuedPackets() {
        return queue;
    }


    /**
     * When a packet arrives *at this node*, we notify
     * both node-specific logic (onDelivered) *and* the
     * global listener.
     */
    public void deliver(Packet p) {
        onDelivered(p);
        if (packetEventListener != null) {
            packetEventListener.onDelivered(p);
        }
    }

    /** When a packet is lost at this node, same deal. */
    public void lose(Packet p) {
        onLost(p);
        if (packetEventListener != null) {
            packetEventListener.onLost(p);
        }
    }

    public abstract Node copy();          // deep copy for snapshots

    /** Debug helper previously printed to console. */
    public void getPortsPrinted() {
        System.out.println(this + " ports=" + inputs.size()+"+"+outputs.size());
    }
    /* ------------------------------------------------ debugging helper */
    public void dumpPorts() {
        System.out.println("x="+(int) position.x()+"  y="+(int) position.y());
        for (Port p : getPorts()) {
            Vector2D rel = new Vector2D(p.getPosition().x() - position.x(),
                    p.getPosition().y() - position.y());
            System.out.printf("   %-6s %-6s  abs=(%.0f,%.0f)  rel=(%.0f,%.0f)%n",
                    p.getDirection(), p.getType(),
                    p.getPosition().x(), p.getPosition().y(),
                    rel.x(), rel.y());
        }
    }
    public NodeSnapshot toSnapshot() { return NodeSnapshot.of(this); }


}
