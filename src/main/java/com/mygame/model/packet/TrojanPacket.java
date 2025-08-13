package com.mygame.model.packet;

import com.mygame.core.GameConfig;

public class TrojanPacket extends Packet{
    private final Packet original;   // 🔁 original version before corruption



    public TrojanPacket(Packet original) {
        super(original.getPosition().copy(), original.health, GameConfig.trojanPacketSize);
        this.original = original;
        this.payloadSize = original.sizeUnits();       // preserve key traits
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
        original.cleanInfection();
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
