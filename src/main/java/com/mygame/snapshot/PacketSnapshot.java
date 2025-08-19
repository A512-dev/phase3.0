package com.mygame.snapshot;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.Port;
import com.mygame.model.packet.Packet;

// In com.mygame.snapshot.PacketSnapshot
public record PacketSnapshot(
        Vector2D position,
        double size,
        double opacity,
        Packet.Shape shape,
        boolean isMobile,
        Vector2D pathStart,
        Vector2D pathEnd,
        Port.PortType fromType,
        Port.PortType toType,
        boolean protectedByVPN,
        boolean trojan,
        boolean bit
) {
    public static PacketSnapshot of(Packet p) {
        Vector2D pathStart = (p.getFromPort() != null) ? p.getFromPort().getCenter() : p.getPosition();
        Vector2D pathEnd = (p.getToPort() != null) ? p.getToPort().getCenter() : p.getPosition();
        Port.PortType fromType = (p.getFromPort() != null) ? p.getFromPort().getType() : null;
        Port.PortType toType = (p.getToPort() != null) ? p.getToPort().getType() : null;

        return new PacketSnapshot(
                p.getPosition().copy(),
                p.getRadius(),
                p.getOpacity(),
                p.shape(),
                p.isMobile(),
                pathStart.copy(),
                pathEnd.copy(),
                fromType,
                toType,
                p.isProtectedPacket(),
                p.isTrojanPacket(),
                p.isBitPacket()
        );
    }


}