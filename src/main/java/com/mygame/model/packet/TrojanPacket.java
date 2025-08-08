package com.mygame.model.packet;

import com.mygame.engine.physics.Vector2D;
import com.mygame.model.packet.messengerPacket.MessengerPacket;

public class TrojanPacket extends Packet{
    private final Packet original;   // 🔁 original version before corruption



    protected TrojanPacket(Packet original) {
        super(original.getPosition().copy(), original.health);
        this.original = original;
        this.sizeUnits = original.sizeUnits();       // preserve key traits
        this.heavyId = original.heavyId();
        this.protectedByVPN = original.isProtectedPacket();
    }
    public Packet revert() {
        // optional: set restored state
        original.setPosition(this.getPosition().copy());
        original.setVelocity(this.getVelocity().copy());
        original.setAcceleration(this.getAcceleration().copy());
        original.setMobile(this.isMobile());
        original.setAlive(this.isAlive());
        original.setOpacity(this.getOpacity());
        return original;
    }
    public Packet getOriginalPacket() {
        return original;
    }
    @Override
    public int getCoinValue() {
        return 0; // Trojan packets give no coin
    }

    @Override
    public Shape shape() {
        return Shape.TROJAN;  // visually distinct
    }

    @Override
    public Packet copy() {
        return new TrojanPacket(original.copy());
    }

    @Override
    public void onCollision(Packet other) {
        super.onCollision(other);  // or sabotage logic
    }
}
