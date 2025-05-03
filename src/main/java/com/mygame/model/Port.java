package com.mygame.model;

import com.mygame.util.Vector2D;

public class Port {
    public enum Type { SQUARE, TRIANGLE }

    private final Vector2D position;
    private final Type type;
    private final SystemNode owner;

    public Port(SystemNode systemNode, Vector2D position, Type type) {
        this.owner = systemNode;
        this.position = position;
        this.type = type;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Type getType() {
        return type;
    }
    public Port copy(SystemNode newOwner) {
        return new Port(newOwner, position.copy(), type);  // or however your constructor looks
    }

}
