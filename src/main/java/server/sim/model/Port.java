package server.sim.model;

import server.sim.core.GameConfig;
import server.sim.engine.physics.Vector2D;
import server.sim.model.node.Node;
import server.sim.model.node.BasicNode;
import server.sim.model.packet.Packet;

public class Port {

    public enum PortType { SQUARE, TRIANGLE, INFINITY }
    public enum PortDirection { OUTPUT, INPUT }

    private final Vector2D position;
    private final PortType type;
    private final PortDirection direction;
    private final Node owner;

    private final GameConfig cfg = GameConfig.defaultConfig();

    private double busyTimer = 0;
    // In Port.java
    private double emitCooldown = 1/GameConfig.defaultConfig().timeMultiplier;     // seconds between packets
    private double timeUntilNextEmit = 0;  // counts down
    private Port connectedPort;
    /** The Connection object this port participates in (if any). */
    private Connection wire;

    public Port(Node owner, PortType type, Vector2D position, PortDirection direction) {
        this.owner = owner;
        this.type = type;
        this.direction = direction;
        this.position = position;
    }
    /** Wire‐back‐pointer: which Connection this port is on */
    public Connection getWire() { return wire; }
    public void setWire(Connection wire) {
        if (wire==null) {
            this.wire = null; return;
        }
        this.wire = wire;
        if (this.equals(wire.getFrom()))
            this.connectedPort = wire.getTo();
        else
            this.connectedPort = wire.getFrom();
    }

    /** Hand this packet straight into the node’s delivery logic. */
    public void deliver(Packet p) {
        owner.onDelivered(p, this);
    }



    public boolean canEmit() {
        return timeUntilNextEmit <= 0;
    }
    public boolean isEmitting() {
        return timeUntilNextEmit > 0;
    }

    public void tickCooldown(double dt) {
        if (timeUntilNextEmit > 0)
            timeUntilNextEmit -= dt;
    }

    public void resetCooldown() {
        timeUntilNextEmit = emitCooldown;
    }
    public void setEmitCooldown(double seconds) {
        this.emitCooldown = seconds;
    }
    public Port getConnectedPort() {
        return connectedPort;
    }

    public void setConnectedPort(Port connectedPort) {
        this.connectedPort = connectedPort;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getCenter() {
        return new Vector2D(position.x() + cfg.portSize / 2.0, position.y() + cfg.portSize / 2.0);
    }

    public PortType getType() {
        return type;
    }

    public PortDirection getDirection() {
        return direction;
    }

    public Port copy(BasicNode newOwner) {
        return new Port(newOwner, type, position.copy(),direction);
    }

    public Node getOwner() {
        return owner;
    }
}
