package shared.snapshot;

import shared.Vector2D;
import shared.model.PacketShape;
import shared.model.PortType;

// In shared.snapshot.PacketSnapshot
public record PacketSnapshot(
        Vector2D position,
        double size,
        double health,
        float opacity,
        PacketShape packetShape,
        PacketShape originalPacketShapeIfProtected,
        boolean isMobile,
        Vector2D pathStart,
        Vector2D pathEnd,
        PortType fromType,
        PortType toType,
        boolean protectedByVPN,
        boolean trojan,
        boolean bit,
        int payload

) {
}