//// shared/dto/WireDTO.java
//package shared.dto;
//
//import java.util.List;
//
//public final class WireDTO {
//    public PointDTO from;        // absolute endpoint
//    public PointDTO to;          // absolute endpoint
//    public String   fromType;    // e.g., "IN", "OUT" (stringified enum)
//    public String   toType;      // e.g., "IN", "OUT"
//    public List<PointDTO> path;  // includes bends in order (may be empty)
//
//    public static final class PointDTO {
//        public double x, y;
//        public PointDTO() {}
//        public PointDTO(double x, double y) { this.x = x; this.y = y; }
//    }
//
//    public WireDTO() {}
//}
