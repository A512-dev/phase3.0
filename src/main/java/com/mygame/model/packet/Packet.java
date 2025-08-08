// Packet.java
package com.mygame.model.packet;

import com.mygame.core.GameConfig;
import com.mygame.engine.physics.PhysicsBody;
import com.mygame.engine.physics.Vector2D;
import com.mygame.engine.physics.MovementStrategy;
import com.mygame.model.Connection;
import com.mygame.model.Port;
import com.mygame.model.packet.bulkPacket.BitPacket;
import com.mygame.model.packet.confidentialPacket.ConfidentialPacket;

public abstract class Packet implements PhysicsBody {
    public enum Shape { SQUARE, TRIANGLE, INFINITY, HEXAGON, LOCK, BULK_A, CONFIDENTIAL_A, CONFIDENTIAL_B, BULK_B , TROJAN}




    protected Vector2D pos   = new Vector2D(0,0);
    protected boolean protectedByVPN = false;   // single‑use immunity flag
    protected int      heavyId       = -1;      // group id for Heavy/Bit packets
    protected int      sizeUnits     = 1;       // payload size (≥1)

    // Packet.java (add)
    private Connection wire = null;      // current wire, null = in node / off track
    private double     pathPos = 0;      // distance along wire (for HUD only)

    /* … فیلدهای قبلی … */
    private Vector2D accel = new Vector2D(0, 0);   // ⬅️ شتاب جهانی (px/s²)
    protected Vector2D vel   = new Vector2D(20,0);     // px / s
    protected double   radius = 10;                      // for collision
    protected boolean  mobile = true;

    protected int   health = 1;

    protected final double   size = GameConfig.defaultConfig().packetSize;
    protected        float   opacity = 1f;
    protected        boolean alive  = true;
    protected int life = 2;
    // In com.mygame.model.packet.Packet
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

    protected Packet(Vector2D spawn,int health){ this.pos = spawn.copy(); this.health=health; }




    /** Scalar speed in pixels/second (‖vel‖). */
    public double   speed()     { return vel.length(); }


    /** Simple constant accelerator placeholder. */
    public void setAccelerator(Vector2D acc) { /* for phase-3 power-ups */ }

    /** Tiny “kick” used by CollisionSystem. */
    public Vector2D getImpulse() { return vel.normalized().multiplied(radius*0.1); }

    public int getHealth(){return health;}
    public float    getOpacity(){return opacity;}

    /* ------------ abstract bits ------------ */
    public abstract int getCoinValue();
    public abstract Shape shape();

    public void setAlive(boolean aliveStatus) {
        this.alive = false;
        life = 0;
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
        vel.multiply(-0.5);
    }

    /* ------------------------------------------------ arrive / track-line */

    public boolean hasArrived() {
        return toPort != null && pos.distanceTo(toPort.getCenter()) < radius;
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
    public void    setProtected(boolean b){ protectedByVPN = b; }

    public boolean isTrojanPacket()             { return this instanceof TrojanPacket; }
    public boolean isConfidentialPacket()             { return this instanceof ConfidentialPacket; }
    public boolean isBitPacket()                { return this instanceof BitPacket; }

    public int  heavyId()                 { return heavyId; }
    public int  sizeUnits()               { return sizeUnits; }

    /* default coin value: override per spec *

    /* deep copy for snapshots */
    public abstract Packet copy();


}
