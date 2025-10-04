package server.sim.engine.physics;

public interface PhysicsBody {
    Vector2D getPosition();
    Vector2D getVelocity();
    double        getRadius();
    double        getInvMass();       // 0 ⇒ immovable
    double        getRestitution();
    double        getFriction();
    void setPosition(Vector2D p); // mutate in place
    void setVelocity(Vector2D v);
    boolean       canCollide();       // packets toggled “mobile” return false
}
