package server.sim.model.packet.confidentialPacket.types;

import server.sim.core.GameConfig;
import shared.Vector2D;
import server.sim.model.packet.confidentialPacket.ConfidentialPacket;
import shared.model.PacketShape;

public final class ConfidentialLargePacket extends ConfidentialPacket {

    public ConfidentialLargePacket(Vector2D spawn, double health) {
        super(spawn, health, GameConfig.CONFIDENTIAL_LARGE_PACKET_SIZE); // اندازه 6 واحد
    }

    @Override
    public int getCoinValue() { return GameConfig.CONFIDENTIAL_LARGE_PACKET_COIN_VALUE; }

    @Override
    public PacketShape shape() { return PacketShape.CONFIDENTIAL_L; }

    @Override
    public ConfidentialLargePacket copy() {
        ConfidentialLargePacket cp = new ConfidentialLargePacket(pos.copy(), health);
        cp.setVelocity(vel.copy());
        cp.setAcceleration(getAcceleration().copy());
        return cp;
    }

    // حرکت: سرعت ثابت + حفظ فاصله مشخص با سایر پکت‌های روی سیم (در منطق حرکت اضافه شود)


    @Override public Vector2D getPosition()    { return pos; }
    @Override public Vector2D getVelocity()    { return vel; }
    @Override public double   getRadius()      { return radius; }
    @Override public double   getInvMass()     { return invMass; }
    @Override public double   getRestitution() { return restitution; }
    @Override public double   getFriction()    { return friction; }
    @Override public void     setPosition(Vector2D p) { pos.set(p); }
    @Override public void     setVelocity(Vector2D v) { vel.set(v); }
}
