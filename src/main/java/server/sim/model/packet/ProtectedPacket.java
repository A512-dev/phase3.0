package server.sim.model.packet;

import java.util.Random;

public final class ProtectedPacket extends Packet {
    private final Packet original;  // 🔁 Remember original MessengerPacket
    public enum MovementType {SQUARE, TRIANGLE, INFINITY}

    public MovementType getMovementType() {
        return movementType;
    }

    private final MovementType movementType;


    public ProtectedPacket(Packet original) {
        super(original.getPosition().copy(), original.getHealth() * 2, original.getRadius()); // double health
        this.original = original;
        this.payloadSize = original.sizeUnits();
        this.heavyId = original.heavyId();
        this.protectedByVPN = true;
        Random random = new Random();
        int movementPattern = random.nextInt(3);
        if (movementPattern%3==0)
            movementType = MovementType.SQUARE;
        else if (movementPattern%3==1)
            movementType = MovementType.TRIANGLE;
        else
            movementType = MovementType.INFINITY;

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
    public Packet revert() {
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
    public Packet getOriginal() { return original; }


    @Override
    public ProtectedPacket copy() {
        return new ProtectedPacket(original.copy());
    }
}
