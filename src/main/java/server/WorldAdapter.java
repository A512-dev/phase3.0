//// server/WorldAdapter.java
//package server;
//
//import shared.dto.*;
//import shared.dto.WireDTO.PointDTO;
//
//import shared.Vector2D;
//import shared.snapshot.WorldSnapshot;
//import shared.snapshot.PacketSnapshot;
//import shared.snapshot.ConnectionSnapshot;
//import shared.snapshot.NodeSnapshot;
//import shared.snapshot.PortSnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public final class WorldAdapter {
//    private static long seq = 0;
//    private static long sysCounter = 0; // synthetic ids if NodeSnapshot lacks one
//
//    private WorldAdapter() {}
//
//    /** Preferred: convert a ready WorldSnapshot (already consistent for this tick). */
//    public static WorldFrameDTO toDto(WorldSnapshot s) {
//        return toDto(s, ++seq);
//    }
//
//    public static WorldFrameDTO toDto(WorldSnapshot s, long frameNo) {
//        WorldFrameDTO dto = new WorldFrameDTO();
//        dto.frame    = frameNo;
//        dto.gameTime = s.hud().gameTimeSec();  // adjust if accessor differs
//        dto.gameOver = s.isGameOver();
//        dto.viewOnly = s.isViewOnly();
//
//        // Systems / Nodes (optional if you want them now)
//        if (s.nodes() != null) {
//            dto.systems = new ArrayList<>(s.nodes().size());
//            for (NodeSnapshot n : s.nodes()) {
//                SystemDTO sd = new SystemDTO();
//                sd.id = ++sysCounter; // replace with n.id() if you add it
//                sd.systemType = (n.nodeType() != null) ? n.nodeType().name() : "Unknown";
//                sd.x = n.position().x();
//                sd.y = n.position().y();
//                sd.w = n.width();
//                sd.h = n.height();
//                sd.allConnected = n.isAllConnected();
//                sd.queueSize = (n.queue() != null) ? n.queue().size() : 0;
//
//                // Ports
//                if (n.ports() != null) {
//                    sd.ports = new ArrayList<>(n.ports().size());
//                    for (PortSnapshot p : n.ports()) {
//                        PortDTO pd = new PortDTO();
//                        pd.x = p.position().x();
//                        pd.y = p.position().y();
//                        pd.type      = (p.type()      != null) ? p.type().name()      : null;
//                        pd.direction = (p.direction() != null) ? p.direction().name() : null;
//                        sd.ports.add(pd);
//                    }
//                }
//
//                dto.systems.add(sd);
//            }
//        }
//
//        // Wires / Connections (with bends)
//        dto.wires = new ArrayList<>();
//        for (ConnectionSnapshot c : s.connections()) {
//            WireDTO w = new WireDTO();
//            w.from     = toPoint(c.fromPos());
//            w.to       = toPoint(c.toPos());
//            w.fromType = (c.fromType() != null) ? c.fromType().name() : null;
//            w.toType   = (c.toType()   != null) ? c.toType().name()   : null;
//
//            List<PointDTO> path = new ArrayList<>();
//            if (c.bends() != null) {
//                for (Vector2D b : c.bends()) path.add(new PointDTO(b.x(), b.y()));
//            }
//            w.path = path;
//            dto.wires.add(w);
//        }
//
//        // Packets
//        dto.packets = new ArrayList<>();
//        for (PacketSnapshot p : s.packets()) {
//            PacketDTO pd = PacketDTO.of(p);
//            pd.x = p.position().x();
//            pd.y = p.position().y();
//            pd.size = p.size();
//            pd.opacity = p.opacity();
//            pd.mobile = p.isMobile();
//
//            if (p.trojan())              pd.packetType = "TROJAN";
//            else if (p.protectedByVPN()) pd.packetType = "CONFIDENTIAL";
//            else if (p.bit())            pd.packetType = "BIT";
//            else                         pd.packetType = (p.packetShape() != null ? p.packetShape().name() : "MESSAGE");
//
//            pd.fromType = (p.fromType() != null) ? p.fromType().name() : null;
//            pd.toType   = (p.toType()   != null) ? p.toType().name()   : null;
//
//            pd.protectedByVPN = p.protectedByVPN();
//            pd.trojan         = p.trojan();
//            pd.bit            = p.bit();
//
//            if (p.pathStart() != null) { pd.pathStartX = p.pathStart().x(); pd.pathStartY = p.pathStart().y(); }
//            if (p.pathEnd()   != null) { pd.pathEndX   = p.pathEnd().x();   pd.pathEndY   = p.pathEnd().y();   }
//
//            dto.packets.add(pd);
//        }
//
//        return dto;
//    }
//
//    private static PointDTO toPoint(Vector2D v) {
//        return (v == null) ? null : new PointDTO(v.x(), v.y());
//    }
//}
