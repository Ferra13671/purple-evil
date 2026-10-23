package govno.world.entity;

import org.joml.Vector2d;
import org.joml.Vector2dc;

public class Player extends Entity {
    protected final PlayerInput input = new PlayerInput();

    @Override
    public void tick() {
        super.tick();

        this.input.tick();
        tickMovement();
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

    @Override
    public void onWorldCollision(Vector2dc out) {
        this.position.add(out);

        double len = out.length();
        if (len < 1e-12f)
            return;

        double nx = out.x() / len;
        double ny = out.y() / len;

        double vn = this.velocity.x() * nx + this.velocity.y() * ny;

        if (vn < 0f) {
            this.velocity.add(
                    -nx * vn,
                    -ny * vn
            );
        }
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {

    }
}
