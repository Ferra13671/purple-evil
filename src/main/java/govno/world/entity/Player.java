package govno.world.entity;

import org.joml.Vector2d;

public class Player extends Entity {
    protected final PlayerInput input = new PlayerInput();

    @Override
    public void tick() {
        startTick();

        this.input.tick();
        tickMovement();

        endTick();
    }

    protected void tickMovement() {
        double friction = 0.3;
        double acceleration = 0.60 * friction;
        double speed = 0.6;

        Vector2d inputSpeed = new Vector2d(this.input.vector).normalize().mul(speed);
        if (inputSpeed.lengthSquared() > 0.001f) {
            this.velocity.set(
                    this.velocity.x + (inputSpeed.x - this.velocity.x) * acceleration,
                    this.velocity.y + (inputSpeed.y - this.velocity.y) * acceleration
            );
        } else {
            this.velocity.set(
                    this.velocity.x * (1 - acceleration),
                    this.velocity.y * (1 - acceleration)
            );
        }

        this.velocity.mul(10000).round().mul(1 / 10000d);
    }
}
