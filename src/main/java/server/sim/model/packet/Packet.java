// Packet.java
package server.sim.model.packet;

import server.sim.core.GameConfig;
import server.sim.engine.physics.PhysicsBody;
import shared.Vector2D;
import server.sim.model.Connection;
import server.sim.model.Port;
import server.sim.model.packet.bulkPacket.BitPacket;
import server.sim.model.packet.bulkPacket.types.BulkPacketA;
import server.sim.model.packet.bulkPacket.types.BulkPacketB;
import server.sim.model.packet.confidentialPacket.ConfidentialPacket;
import server.sim.model.packet.confidentialPacket.types.ConfidentialLargePacket;
import server.sim.model.packet.confidentialPacket.types.ConfidentialSmallPacket;
import server.sim.model.packet.messengerPacket.types.InfinityPacket;
import server.sim.model.packet.messengerPacket.types.SquarePacket;
import server.sim.model.packet.messengerPacket.types.TrianglePacket;
import shared.snapshot.PacketSnapshot;
import shared.model.PacketShape;
import shared.model.PortType;

public abstract class Packet implements PhysicsBody {

    protected Packet(Vector2D spawn, double health, double radius){ this.pos = spawn.copy(); this.health=health; this.radius = radius; }

    // --- add fields ---
    private Vector2D impulse = new Vector2D();   // transient "kick"
    private static final double IMPULSE_DECAY = 8.0; // 1/s, tune or move to Database
    private boolean infected = false;
    // ---- health & status ----
    protected int maxHealth = 100;
    protected double health  = maxHealth;

    public static PacketSnapshot of(Packet p) {
        Vector2D pathStart = (p.getFromPort() != null) ? p.getFromPort().getCenter() : p.getPosition();
        Vector2D pathEnd = (p.getToPort() != null) ? p.getToPort().getCenter() : p.getPosition();
        PortType fromType = (p.getFromPort() != null) ? p.getFromPort().getType() : null;
        PortType toType = (p.getToPort() != null) ? p.getToPort().getType() : null;

        return new PacketSnapshot(
                p.getPosition().copy(),
                p.getRadius(),
                p.getHealth(),
                p.getOpacity(),
                p.shape(),
                (p instanceof ProtectedPacket)
                        ? ((ProtectedPacket) p).getOriginal().shape()
                        : p.shape(),
                p.isMobile(),
                pathStart.copy(),
                pathEnd.copy(),
                fromType,
                toType,
                p.isProtectedPacket(),
                p.isTrojanPacket(),
                p.isBitPacket(),
                p.payloadSize
        );

    }

    /** Factory: rehydrate a model Packet from an immutable PacketSnapshot. */
    public static Packet fromSnapshot(PacketSnapshot s) {
        // 1) Choose concrete packet type based on shape/flags
        Packet pkt = switch (s.packetShape()) {
            case SQUARE   ->
                    new SquarePacket(s.position().copy(), s.health(), (int) Math.round(s.size()));
            case TRIANGLE ->
                    new TrianglePacket(s.position().copy(),  s.health(), GameConfig.triangleSize);
            case INFINITY -> new InfinityPacket(s.position().copy(), s.health(), GameConfig.infinitySize);
            case HEXAGON  -> new InfinityPacket(s.position().copy(), s.health()+1.0, GameConfig.infinitySize);
            /* TODO or fall back */
            case BULK_A -> new BulkPacketA(s.position().copy(),s.payload(), s.health());
            case BULK_B -> new BulkPacketB(s.position().copy(),s.payload(), s.health());
            case CONFIDENTIAL_S -> new ConfidentialSmallPacket(s.position().copy(), s.health());
            case CONFIDENTIAL_L -> new ConfidentialLargePacket(s.position().copy(), s.health());
            case LOCK -> {
                if (s.protectedByVPN()) {
                    // make a copy so we can adjust the shape if needed
                    PacketSnapshot z = new PacketSnapshot(
                            s.position(),
                            s.size(),
                            s.health(),
                            s.opacity(),
                            s.originalPacketShapeIfProtected(),
                            s.originalPacketShapeIfProtected(),
                            s.isMobile(),
                            s.pathStart(),
                            s.pathEnd(),
                            s.fromType(),
                            s.toType(),
                            false,
                            s.trojan(),
                            s.bit(),
                            s.payload()
                    );
                    yield new ProtectedPacket(fromSnapshot(z));
                }
                else
                    yield new InfinityPacket(s.position().copy(), s.health(), GameConfig.infinitySize);
            }
            case TROJAN -> {
                if (s.trojan()) {
                    // make a copy so we can adjust the shape if needed
                    PacketSnapshot z = new PacketSnapshot(
                            s.position(),
                            s.size(),
                            s.health(),
                            s.opacity(),
                            s.originalPacketShapeIfProtected(),
                            s.originalPacketShapeIfProtected(),
                            s.isMobile(),
                            s.pathStart(),
                            s.pathEnd(),
                            s.fromType(),
                            s.toType(),
                            s.protectedByVPN(),
                            false,
                            s.bit(),
                            s.payload()
                    );
                    yield new ProtectedPacket(fromSnapshot(z));
                }
                else
                    yield new InfinityPacket(s.position().copy(), s.health(), GameConfig.infinitySize);
            }
            case BIT -> {
                // TODO: 10/6/2025
                yield new InfinityPacket(s.position().copy(), s.health(), GameConfig.infinitySize);
            }

//                    -> defaultBasic(s);
        };

        // 2) Copy shared scalar fields (adjust setter names to your API)
        pkt.setOpacity(s.opacity());
        pkt.setMobile(s.isMobile());
        if (s.protectedByVPN()) pkt.setProtectedPacket(true);
        if (s.trojan())         pkt.setTrojanPacket(true);
        if (s.bit())            pkt.setBitPacket(true);

        // 3) Optional: if your model supports remembering the path endpoints/types
        //    (Only if these exist in your API — otherwise omit)
        // pkt.setPathStart(s.pathStart());  // if you have such methods
        // pkt.setPathEnd(s.pathEnd());
        // pkt.setFromType(s.fromType());
        // pkt.setToType(s.toType());

        return pkt;
    }

    private void setBitPacket(boolean b) {

    }

    private void setTrojanPacket(boolean b) {

    }


    // --- public API ---
    public void addImpulse(Vector2D j) { this.impulse = this.impulse.added(j); }
    public Vector2D getImpulse()       { return impulse; }
    public void     decayImpulse(double dt) {
        // ~50ms TTL at 60 FPS: fast, deterministic fade
        double k = Math.exp(-dt / 0.05);
        impulse = impulse.multiplied(k);
        if (impulse.lengthSq() < 1e-8) impulse = new Vector2D(0,0);
    }
    public void clearImpulse() { impulse = new Vector2D(); }


    public boolean hasNoise() {
        return noise>0;
    };

    public void addNoise(double i) {
        noise += i;
    }


    protected Vector2D pos   = new Vector2D(0,0);

    protected boolean protectedByVPN = false;   // single‑use immunity flag
    protected int      heavyId       = -1;      // group id for Heavy/Bit packets
    protected int payloadSize = 1;       // payload size (≥1)

    // Packet.java (add)
    private Connection wire = null;      // current wire, null = in node / off track
    private double     pathPos = 0;      // distance along wire (for HUD only)

    /* … فیلدهای قبلی … */
    private Vector2D accel = new Vector2D(0, 0);   // ⬅️ شتاب جهانی (px/s²)
    protected Vector2D vel   = new Vector2D(20,0);     // px / s
    protected double   radius = 10;                      // for collision
    protected boolean  mobile = true;
    protected double  noise = 0;

    protected final double   size = GameConfig.defaultConfig().packetSize;
    protected        float   opacity = 1f;
    protected        boolean alive  = true;
    protected int life = 2;

    public void setFromPort(Port fromPort) {
        this.fromPort = fromPort;
    }

    public void setToPort(Port toPort) {
        this.toPort = toPort;
    }


    public void resetMotionForNodeHop() {
        setAcceleration(new Vector2D());
        setVelocity(new Vector2D());
        clearImpulse();
        attachToWire(null); // or setWire(null) if that’s your API
    }


    // In server.sim.model.packet.Packet
    protected Port fromPort;
    protected Port toPort;


    /* ───────── physics traits required by PhysicsBody ───────── */
    protected double invMass      = 1.0;   // 1 / mass (0 ⇒ immovable / ghost)
    protected double restitution  = 0.2;   // bounciness [0‒1]
    protected double friction     = 0.1;   // Coulomb µ  [0‒1]

    /* ------------------------------------------------ basic mutable state */

    /* ───────── interface implementation ───────── */
    @Override public Vector2D getPosition() { return pos; }
    @Override public Vector2D getVelocity() { return vel; }
    @Override public double   getRadius()          { return radius; }
    @Override public double   getInvMass()         { return invMass; }
    @Override public double   getRestitution()     { return restitution; }
    @Override public double   getFriction()        { return friction; }
    @Override public boolean canCollide() { return mobile; }
    @Override public void setPosition(Vector2D p)       { pos.set(p); }
    @Override public void setVelocity(Vector2D v)       { vel.set(v); }
    /* --- getters / setters --- */
    public Vector2D getAcceleration()           { return accel;        }
    public void     setAcceleration(Vector2D a) { this.accel.set(a);   }
    public void     addAcceleration(Vector2D a) { this.accel.add(a);   }

    /* اگر می‌خواهید صفر شود */
    public void clearAcceleration() { this.accel.set(0,0); }
    /** Convenience: change velocity without recreating vector. */
    public void setVelocity(double vx,double vy){ this.vel.setX(vx); this.vel.setY(vy); }




    public void setRoute(Port from, Port to) {
        this.fromPort = from;
        this.toPort = to;
    }

    public Port getFromPort() { return fromPort; }
    public Port getToPort() { return toPort; }






    /** Scalar speed in pixels/second (‖vel‖). */
    public double   speed()     { return vel.length(); }


//    /** Simple constant accelerator placeholder. */
//    public void setAccelerator(Vector2D acc) { /* for phase-3 power-ups */ }

    public double getHealth(){return health;}
    public float    getOpacity(){return opacity;}

    /* ------------ abstract bits ------------ */
    public abstract int getCoinValue();
    public abstract PacketShape shape();

    public void setAlive(boolean aliveStatus) {
        if (aliveStatus)
            this.alive = true;
        else {
            this.alive = false;
            life = 0;
        }
    }

    public void setOpacity(float v) {
        this.opacity = v;
    }





//    public void   advance(double dx){ pos.add(vel.x()*dx, vel.y()*dx);}
////    public void   update(double dt){
////        if(mobile)
////            move.update(this,dt);
////    }



    public void  attachToWire(Connection w) { wire = w; pathPos = 0; }
    public void  detachFromWire()           { wire = null;          }
    public Connection getWire()             { return wire; }

    /* ------------------------------------------------ collision hooks */



    public void onCollision(Packet other) {
        // base: reverse velocity a bit
        vel.multiply(-0.2);
    }

    /* ------------------------------------------------ arrive / track-line */

    public boolean hasArrived() {
        boolean arrived = toPort != null && pos.distanceTo(toPort.getCenter()) < radius;
        if (arrived) {
            System.out.println("toport = "+ toPort);
            System.out.println("pos.distanceTo(toPort.getCenter()) < radius===" + (pos.distanceTo(toPort.getCenter()) < radius));
        }
        return arrived;
    }
    public Vector2D getPathEnd() { return pos; }         // used by HUD
    public boolean isOffTrackLine(double max, Vector2D positon) {
        return false;
    }


    /* ------------------------------------------------ misc */

    public void resetHealth() { health = 1; }
    public boolean isMobile() { return mobile; }
    public void setMobile(boolean b) { mobile = b; }
    public boolean isAlive(){ return life>0; }




    /* convenience helpers used by multiple node types */
    public boolean isProtectedPacket()           { return protectedByVPN; }
    public void setProtectedPacket(boolean b){ protectedByVPN = b; }

    /* =====================  health & status  ===================== */

    public boolean isInfected() { return infected; }
    public void infect()        { infected = true; }
    public void cleanInfection(){ infected = false; }  // ✅ used by AntiTrojan etc.

    public boolean isTrojanPacket()             { return this instanceof TrojanPacket; }

    public boolean isConfidentialPacket()             { return this instanceof ConfidentialPacket; }
    public boolean isBitPacket()                { return this instanceof BitPacket; }

    public int  heavyId()                 { return heavyId; }
    public int  sizeUnits()               { return payloadSize; }

    /** Reduce health; when <= 0 mark dead. */
    public void damage(double amount) {
        if (amount <= 0) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            setAlive(false);     // assumes Packet already has setAlive(boolean)
        }
    }

    /** Increase health but clamp to max. */
    public void heal(double amount) {
        if (amount <= 0) return;
        health = Math.min(maxHealth, health + amount);
    }

    /* default coin value: override per spec *

    /* deep copy for snapshots */
    public abstract Packet copy();


    // Packet.java
    private static int NEXT_ID = 1;
    private final int id = NEXT_ID++;
    private final java.util.Set<String> tags = new java.util.HashSet<>();

    public int getId(){ return id; }

    public boolean hasTag(String t){ return tags.contains(t); }
    public void addTag(String t){ tags.add(t); }
    public void removeTag(String t){ tags.remove(t); }


}
