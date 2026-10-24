package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.Direction;
import govno.world.attribute.Attributes;
import govno.world.attribute.EntityAttributes;
import govno.world.entity.input.Input;
import lombok.Getter;
import lombok.NonNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class LivingEntity extends Entity {
    protected final Input input;
    protected final Attributes attributes;
    @Getter
    protected Direction facing = Direction.DOWN;
    @Getter
    protected double health;


    public LivingEntity(@NonNull EntityModel model, @NonNull Input input, @NonNull Attributes attributes, double health) {
        super(model);

        this.input = input;
        this.attributes = attributes;
        this.health = Math.clamp(health, 0, getAttributeValue(EntityAttributes.MAX_HEALTH));
    }

    public double getAttributeValue(String id) {
        return this.attributes.getCurrentValue(id);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.health < getAttributeValue(EntityAttributes.MAX_HEALTH))
            this.health = Math.min(this.health + getAttributeValue(EntityAttributes.REGENERATION), getAttributeValue(EntityAttributes.MAX_HEALTH));

        this.input.tick();
        tickMovement();
    }

    public void damage(double damage) {
        this.health -= damage;
    }

    private void updateFacing() {
        Vector2d in = this.input.vector;
        if (in.lengthSquared() < 0.001) return;

        if (Math.abs(in.x) >= Math.abs(in.y)) {
            this.facing = in.x > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            this.facing = in.y > 0 ? Direction.UP : Direction.DOWN;
        }

        if (this.model != null)
            this.model.setDirection(this.facing);
    }

    protected void tickMovement() {
        double friction = 0.3;
        double acceleration = getAttributeValue(EntityAttributes.ACCELERATION) * friction;

        updateFacing();

        Vector2d inputSpeed = new Vector2d(this.input.vector).normalize().mul(getAttributeValue(EntityAttributes.SPEED));
        if (inputSpeed.lengthSquared() > 0.001f) {
            this.velocity.set(
                    this.velocity.x + (inputSpeed.x - this.velocity.x) * acceleration,
                    this.velocity.y + (inputSpeed.y - this.velocity.y) * acceleration
            );

            if (this.model != null)
                this.model.play("go");
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
