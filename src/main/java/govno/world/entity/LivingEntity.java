package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.utils.Direction;
import govno.utils.Utils;
import govno.world.attribute.Attributes;
import govno.world.entity.input.Input;
import govno.world.weapon.Weapon;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.lwjgl.glfw.GLFW;

public class LivingEntity extends Entity {
    @NonNull
    @Setter
    protected Input input = new Input();
    protected final Attributes attributes;
    @Getter
    protected Direction facing = Direction.DOWN;
    @Getter
    @Setter
    protected double health;
    @Getter
    protected Weapon weapon;
    @Getter
    protected boolean usingWeapon = false;
    @Getter
    protected final Vector2d handPos;

    @Getter
    @Setter
    protected double invincibilityTimer = 0;

    public LivingEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, double health, Vector2d handPos, Weapon weapon) {
        super(model, aabb);

        this.attributes = attributes;
        this.health = Math.clamp(health, 0, getAttributeValue(EntityAttributes.MAX_HEALTH));
        this.handPos = handPos;
        setWeapon(weapon);
    }

    public double getAttributeValue(String id) {
        return this.attributes.getCurrentValue(id);
    }

    public void setWeapon(Weapon weapon) {
        if (this.weapon != null)
            this.weapon.getEntityModifiers().forEach((attribute, modifier) -> this.attributes.removeModifier(attribute, modifier.id()));

        this.weapon = weapon;
        if (this.weapon != null)
            this.weapon.getEntityModifiers().forEach(this.attributes::addModifier);
    }

    public double getRenderYaw() {
        return Utils.lerp(this.prevYaw, this.yaw, game.getTicker().getTickDelta());
    }

    public void onDeath() {
        game.getWorld().removeEntity(this);
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.weapon != null) {
            if (game.getWindow().isPressedMouse(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                if (!this.usingWeapon) {
                    this.usingWeapon = true;
                    this.weapon.startUse(this);
                }
            } else if (this.usingWeapon) {
                this.usingWeapon = false;
                this.weapon.endUse(this);
            }
        }

        if (this.health <= 0)
            onDeath();

        if (this.health < getAttributeValue(EntityAttributes.MAX_HEALTH))
            this.health = Math.min(this.health + getAttributeValue(EntityAttributes.REGENERATION), getAttributeValue(EntityAttributes.MAX_HEALTH));

        if (this.invincibilityTimer > 0) {
            this.invincibilityTimer -= game.getTicker().getTickDelay() / 1000.0;
            if (this.invincibilityTimer < 0) {
                this.invincibilityTimer = 0;
            }
        }

        this.input.tick();
        tickMovement();
    }

    public void damage(double damage) {
        if (isInvincible())
            return;
        this.health -= damage;

        this.invincibilityTimer = getAttributeValue(EntityAttributes.INVINCIBILITY_TIME);
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
        onCollision(out);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other instanceof LivingEntity && !(other instanceof BatEntity))
            onCollision(new Vector2d(out).mul(0.5f));
    }

    protected void onCollision(Vector2dc out) {
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
}
