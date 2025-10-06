//// shared/dto/SystemDTO.java
//package shared.dto;
//
//import java.util.List;
//
///** Engine-agnostic, JSON-friendly node/system snapshot. */
//public final class SystemDTO {
//    public long   id;             // stable if available; synthetic otherwise
//    public String systemType;     // e.g., "BasicNode", "SpyNode", ...
//    public double x, y;           // top-left or anchor (match engine convention)
//    public int    w, h;           // width/height in pixels (or logical units)
//
//    public boolean allConnected;  // mirrors isAllConnected
//    public int     queueSize;     // size only (avoid engine Packet types)
//
//    public List<PortDTO> ports;   // ports rendered with absolute positions
//
//    public SystemDTO() {}
//}
