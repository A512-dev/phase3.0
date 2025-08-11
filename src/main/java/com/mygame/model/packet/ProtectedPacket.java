package com.mygame.model.packet;

import com.mygame.model.packet.messengerPacket.MessengerPacket;

public final class ProtectedPacket extends Packet {
    private final MessengerPacket original;  // 🔁 Remember original MessengerPacket

    public ProtectedPacket(MessengerPacket original) {
        super(original.getPosition().copy(), original.getHealth() * 2); // double health
        this.original = original;
        this.sizeUnits = original.sizeUnits();
        this.heavyId = original.heavyId();
        this.protectedByVPN = true;
    }

    /** Pick one messenger shape at random to mask true identity */
    @Override
    public Shape shape() {
        return Shape.LOCK;
    }

    @Override
    public int getCoinValue() {
        return 5;
    }

    /** Used when reverting the packet */
    public MessengerPacket revert() {
        // Restore position, velocity, etc.
        original.setPosition(this.getPosition().copy());
        original.setVelocity(this.getVelocity().copy());
        original.setAcceleration(this.getAcceleration().copy());
        original.setMobile(this.isMobile());
        original.setAlive(this.isAlive());
        original.setOpacity(this.getOpacity());
        return original;
    }

    /** (optional) expose the wrapped original if you need it elsewhere */
    public MessengerPacket getOriginal() { return original; }


    @Override
    public ProtectedPacket copy() {
        return new ProtectedPacket(original.copy());
    }
}
