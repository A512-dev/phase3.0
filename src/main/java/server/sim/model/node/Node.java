package server.sim.model.node;

import server.sim.engine.debug.LossDebug;
import shared.Vector2D;
import server.sim.model.PacketEventListener;
import server.sim.model.Port;
import server.sim.model.packet.Packet;
import server.sim.model.packet.ProtectedPacket;
import server.sim.model.packet.TrojanPacket;
import server.sim.model.packet.bulkPacket.BulkPacket;
import server.sim.model.packet.messengerPacket.types.SquarePacket;
import server.sim.model.packet.messengerPacket.types.TrianglePacket;
import shared.snapshot.NodeSnapshot;
import shared.model.NodeType;
import shared.model.PortDirection;
import shared.model.PortType;

import java.util.*;
import java.util.stream.Collectors;

/** Core data + API every network node shares. */
public abstract class Node implements PacketEventListener {

    boolean baseLeft = false;
    public void setBaseLeft(boolean b){ baseLeft = b; }
    public boolean isBaseLeft()       { return baseLeft; }

    /** Factory – called by the World when it builds the frame snapshot */
    public static NodeSnapshot of(Node n) {
        return new NodeSnapshot(
                n.getPosition().copy(),
                (int) n.getWidth(),
                (int) n.getHeight(),
                n.getPorts().stream()
                        .map(Port::of)
                        .toList(),
                (n.getNodeType()),          // ✅ model → shared enum
                n.isAllConnected(),
                n.isBaseLeft(),
                n.getQueuedPackets() == null
                        ? List.of()
                        : n.getQueuedPackets().stream()         // ✅ live → snapshot
                        .map(Packet::of)
                        .toList()
        );
    }

    public boolean isOnline() {
        return online;
    }

    private boolean online = true;
    /** Toggle this Node on/off (when off, it won't do new packets). */
    public void setOnline(boolean v) { this.online = v; }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    NodeType nodeType;

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



    public void addInputPort(PortType t, Vector2D relPos){
        Vector2D abs = new Vector2D(position.x() + relPos.x(),
                position.y() + relPos.y());
        Port p = new Port(this, t, abs, PortDirection.INPUT);
        inputs.add(p);
    }
    public void addOutputPort(PortType t, Vector2D relPos){
        Vector2D abs = new Vector2D(position.x() + relPos.x(),
                position.y() + relPos.y());
        Port p = new Port(this, t, abs, PortDirection.OUTPUT);
        outputs.add(p);
    }

    /* ── queue helpers ────────────────────────────────────── */
    public void enqueuePacket(Packet p){
        if (queue.size()>=5) {
            LossDebug.mark(p, "QUEUE_OVERFLOW at " + getClass().getSimpleName());
            p.setAlive(false);
        }
        else
            queue.addLast(p);
    }
    // Node.java (base class)
    public void emitFrom(int outPortIndex) {
        if (queue.isEmpty()) return;
        Packet pkt = queue.removeFirst(); // or remove(0)
        Port out = getOutputs().get(outPortIndex);
        var wire = out.getWire();
        if (wire == null) {
            // safe guard to avoid NPE; requeue and bail
            queue.addFirst(pkt);
            System.err.println("emitFrom: no wire on port " + outPortIndex + " of " + this);
            return;
        }
        wire.transmit(pkt); // or transmit(pkt) depending on your API
    }

    protected void emitQueued(List<Packet> worldPackets){
        if(!queue.isEmpty()){
            Packet p = queue.peekFirst();
            if (p instanceof SquarePacket) {
                List<Port> squarePorts = outputs.stream()
                        .filter(port -> port.getType() == PortType.SQUARE)
                        .collect(Collectors.toList());

                if (squarePorts.size()>0) {
                    for (Port out : squarePorts) {
                        if (out.getConnectedPort() != null && out.canEmit()) {
                            out.getWire().transmit(p);
                            p.setMobile(true);
                            worldPackets.add(p);
                            queue.remove(p);
                            out.resetCooldown();
                            return;                       // emit one packet per update
                        }
                    }
                }
            }
            if (p instanceof TrianglePacket) {
                List<Port> trianglePorts = outputs.stream()
                        .filter(port -> port.getType() == PortType.TRIANGLE)
                        .collect(Collectors.toList());

                if (trianglePorts.size()>0) {
                    for (Port out : trianglePorts) {
                        if (out.getConnectedPort() != null && out.canEmit()) {
                            out.getWire().transmit(p);
                            p.setMobile(true);
                            worldPackets.add(p);
                            queue.remove(p);
                            out.resetCooldown();
                            return;                       // emit one packet per update
                        }
                    }
                }
            }
            if (p instanceof TrojanPacket) {
                if (((TrojanPacket) p).getOriginalPacket() instanceof SquarePacket) {
                    List<Port> trianglePorts = outputs.stream()
                            .filter(port -> port.getType() == PortType.TRIANGLE)
                            .collect(Collectors.toList());

                    if (trianglePorts.size()>0) {
                        for (Port out : trianglePorts) {
                            if (out.getConnectedPort() != null && out.canEmit()) {
                                out.getWire().transmit(p);
                                p.setMobile(true);
                                worldPackets.add(p);
                                queue.remove(p);
                                out.resetCooldown();
                                return;                       // emit one packet per update
                            }
                        }
                    }
                }
                else if (((TrojanPacket) p).getOriginalPacket() instanceof TrianglePacket) {
                    List<Port> squarePorts = outputs.stream()
                            .filter(port -> port.getType() == PortType.SQUARE)
                            .collect(Collectors.toList());

                    if (squarePorts.size()>0) {
                        for (Port out : squarePorts) {
                            if (out.getConnectedPort() != null && out.canEmit()) {
                                out.getWire().transmit(p);
                                p.setMobile(true);
                                worldPackets.add(p);
                                queue.remove(p);
                                out.resetCooldown();
                                return;                       // emit one packet per update
                            }
                        }
                    }
                }
            }
            if (p instanceof ProtectedPacket) {
                for (Port out : outputs) {
                    if (out.getConnectedPort() != null && out.canEmit()) {
                        out.getWire().transmit(p);
                        p.setMobile(true);
                        worldPackets.add(p);
                        queue.remove(p);
                        out.resetCooldown();
                        return;                       // emit one packet per update
                    }
                }
            }

            for (Port out : outputs) {
                if (out.getConnectedPort() != null && out.canEmit()) {
                    p.setMobile(true);

                    out.getWire().transmit(p);
                    worldPackets.add(p);
                    queue.remove(p);
                    out.resetCooldown();
                    break;                       // emit one packet per update
                }
            }
        }
    }


    /* ── hooks every concrete node must implement ─────────── */
    /** Called exactly once when packet *enters* this node. */
    public abstract void onDelivered(Packet p);
    public void onDelivered(Packet p, Port port) {
        if (p instanceof BulkPacket) {
            queue.clear();
            queue.add(p);
        }
//        if (packetEventListener != null) {
//            packetEventListener.onDelivered(p);
//        }
    }
    public abstract void onLost(Packet p);



    /** Called each frame. Subclass usually calls emitQueued(). */
    public void update(double dt, List<Packet> worldPackets) {
        for (Port port: getPorts()) {
            if (port.isEmitting()) {
                port.tickCooldown(0.6*dt);
                //System.out.println("port CoolDown subtracted dt");
            }
        }
        if (!queue.isEmpty()) {
            emitQueued(worldPackets);

            //System.out.println("Node Emitted Queued");
        }
    }

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
    public NodeSnapshot toSnapshot() { return of(this); }


    protected boolean active = true;
    protected boolean isActive() {
        return active;
    };
    protected void setActive(boolean active) {
        this.active = active;
    };


    protected PacketEventListener getPacketEventListener() { return this.packetEventListener; }
}
