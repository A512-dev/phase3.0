package com.mygame.model;

import com.mygame.util.Database;
import com.mygame.util.Vector2D;

public class Port {


    public enum PortType { SQUARE, TRIANGLE }
    public enum PortDirection { OUTPUT, INPUT}

    private final Vector2D position;
    private final PortType type;
    private final SystemNode owner;

    public PortDirection getDirection() {
        return direction;
    }

    private final PortDirection direction;
    private boolean busy = false;
    private double busyTimer = 0;

    public void updateBusyTimer(double dt, double busyDuration) {
        if (busy) {
            busyTimer += dt;
            if (busyTimer >= busyDuration) {
                busy = false;
                busyTimer = 0;
            }
        }
    }

    private Port connectedPort;

    public Port getConnectedPort() {
        return connectedPort;
    }

    public void setConnectedPort(Port connectedPort) {
        this.connectedPort = connectedPort;
    }



    public Port(SystemNode owner, PortType type, PortDirection direction, Vector2D position) {
        this.owner = owner;
        this.type = type;
        this.direction = direction;
        this.position = position;
    }
    public boolean isBusy() { return busy; }
    public void setBusy(boolean b) { busy = b; }


    public Vector2D getPosition() {
        return position;
    }
    public Vector2D getCenter() {
        return new Vector2D(getPosition().x+ Database.PORT_SIZE/2, getPosition().y+ Database.PORT_SIZE/2);
    }

    public PortType getType() {
        return type;
    }
    public Port copy(SystemNode newOwner) {
        return new Port(newOwner,type,  direction, position.copy());  // or however your constructor looks
    }
    public SystemNode getOwner() {
        return owner;
    }

}
