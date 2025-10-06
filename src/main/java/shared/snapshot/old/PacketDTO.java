//// shared/dto/PacketDTO.java
//package shared.dto;
//
//import shared.Vector2D;
//import server.sim.model.Port;
//import server.sim.model.packet.Packet;
//import shared.model.PacketShape;
//
//public record PacketDTO (
//    Vector2D position,
//    double size,
//    double opacity,
//    PacketShape packetShape,
//    boolean isMobile,
//    Vector2D pathStart,
//    Vector2D pathEnd,
//    Port.PortType fromType,
//    Port.PortType toType,
//    boolean protectedByVPN,
//    boolean trojan,
//    boolean bit
//) {
//    public static PacketDTO of(Packet p) {
//        Vector2D pathStart = (p.getFromPort() != null) ? p.getFromPort().getCenter() : p.getPosition();
//        Vector2D pathEnd = (p.getToPort() != null) ? p.getToPort().getCenter() : p.getPosition();
//        Port.PortType fromType = (p.getFromPort() != null) ? p.getFromPort().getType() : null;
//        Port.PortType toType = (p.getToPort() != null) ? p.getToPort().getType() : null;
//
//        return new PacketDTO(
//                p.getPosition().copy(),
//                p.getRadius(),
//                p.getOpacity(),
//                p.shape(),
//                p.isMobile(),
//                pathStart.copy(),
//                pathEnd.copy(),
//                fromType,
//                toType,
//                p.isProtectedPacket(),
//                p.isTrojanPacket(),
//                p.isBitPacket()
//        );
//    }
//}
