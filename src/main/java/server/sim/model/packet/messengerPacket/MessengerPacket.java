package server.sim.model.packet.messengerPacket;

import shared.Vector2D;
import server.sim.model.packet.Packet;
import shared.model.PacketShape;

public class MessengerPacket extends Packet {
    private final PacketShape packetShape;
    public MessengerPacket(Vector2D spawn, PacketShape packetShape, double radius) {
        super(spawn, 1, radius);
        this.packetShape = packetShape;
    }

    @Override
    public int getCoinValue() {
        return 1;
    }


    @Override
    public PacketShape shape() {
        return packetShape;
    }
    @Override public MessengerPacket copy(){
        MessengerPacket clone = new MessengerPacket(pos.copy(), packetShape, radius);
        clone.setVelocity(this.getVelocity().copy());
        clone.setAcceleration(this.getAcceleration().copy());
        clone.setMobile(this.isMobile());
        clone.setAlive(this.isAlive());
        clone.setOpacity(this.getOpacity());
        return clone;    }
}
