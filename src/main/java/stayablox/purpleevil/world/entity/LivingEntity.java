package stayablox.purpleevil.world.entity;

import com.ferra13671.overtone.SoundBuffer;
import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.utils.Direction;
import stayablox.purpleevil.utils.Utils;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.Input;
import stayablox.purpleevil.world.weapon.Weapon;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.concurrent.ThreadLocalRandom;

public class LivingEntity extends Entity {
    @NonNull
    @Setter
    protected Input input = new Input();
    @Getter
    protected final Attributes attributes;
    @Getter
    protected Direction facing = Direction.DOWN;
    @Getter
    @Setter
    protected double health;
    @Getter
    protected Weapon weapon;
    @Getter
    protected final Vector2d handPos;

    @Getter
    protected boolean dying = false;

    @Getter
    @Setter
    protected double invincibilityTimer = 0;

    @Getter
    @Setter
    private float speedMultiplier = 1.0f;

    @Getter
    protected double shockTimer = 0;

    @Getter
    protected int poisonTimer = 0;
    protected int poisonTickTimer = 0;
    protected double poisonLevel = 0;

    public LivingEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, Vector2d handPos, Weapon weapon) {
        super(model, aabb);

        this.attributes = attributes;
        this.health = getAttributeValue(EntityAttributes.MAX_HEALTH);
        this.handPos = handPos;
        setWeapon(weapon);
    }

    public double getAttributeValue(String id) {
        return this.attributes.getCurrentValue(id);
    }

    public boolean isShocked() {
        return shockTimer > 0;
    }

    public void applyShock(double seconds) {
        this.shockTimer = Math.max(this.shockTimer, seconds);
    }

    public void setWeapon(Weapon weapon) {
        if (this.weapon != null)
            this.weapon.getEntityModifiers().forEach(this.attributes::removeModifier);

        this.weapon = weapon;
        if (this.weapon != null)
            this.weapon.getEntityModifiers().forEach(this.attributes::addModifier);
    }

    public double getRenderYaw() {
        return Utils.lerp(this.prevYaw, this.yaw, game.getTicker().getTickDelta());
    }

    public void applyPoison(double poisonLevel, int durationTicks) {
        this.poisonLevel = poisonLevel;
        this.poisonTimer = durationTicks;

        if (this.poisonTickTimer <= 0)
            this.poisonTickTimer = 20;
    }

    public void onDying() {
        this.dying = true;
        this.velocity.set(0, 0);
        this.poisonTimer = 0;
        this.phasing = false;
        this.speedMultiplier = 1f;
        this.shockTimer = 0;

        if (this.model != null && this.model.hasAnimation("death"))
            this.model.play("death", true);
    }

    public void onDead() {
        game.getWorld().removeEntity(this);
    }

    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    public void revive() {
        this.dying = false;
        this.removed = false;
        this.invincibilityTimer = 0;
        this.velocity.set(0, 0);
        this.input.vector.set(0, 0);
        this.phasing = false;
        this.speedMultiplier = 1f;
        this.shockTimer = 0;

        if (this.model != null)
            this.model.play("stay");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.dying) {
            if (this.model == null || !this.model.hasAnimation("death") || this.model.isFinished()) {
                if (!this.removed)
                    onDead();
            }

            this.velocity.set(0);

            return;
        }

        if (this.weapon != null)
            this.weapon.tick(this);

        if (this.model != null) {
            float dt = game.getTicker().getTickDelay() / 1000f;
            float animSpeed = 1.0f;

            // Если это живая сущность, проверяем её скорость
            if (this.model.getCurrentAnimationName().equals("go")) {
                double speed = this.velocity.length();

                animSpeed = (float) (speed * 4.0);
                animSpeed = Math.clamp(animSpeed, 0f, 3.0f);
            }

            this.model.tick(dt * animSpeed);
        }

        if (this.poisonTimer > 0) {
            this.poisonTimer--;
            this.poisonTickTimer--;

            if (this.poisonTickTimer <= 0) {
                damage(this.poisonLevel * 5);
                this.poisonTickTimer = 20;
            }
        }

        if (this.health <= 0 && !this.dying) {
            onDying();
            return;
        }

        if (this.health < getAttributeValue(EntityAttributes.MAX_HEALTH))
            this.health = Math.min(this.health + getAttributeValue(EntityAttributes.REGENERATION), getAttributeValue(EntityAttributes.MAX_HEALTH));

        if (this.invincibilityTimer > 0) {
            this.invincibilityTimer -= game.getTicker().getTickDelay() / 1000.0;
            if (this.invincibilityTimer < 0) {
                this.invincibilityTimer = 0;
            }
        }

        if (this.shockTimer > 0) {
            this.shockTimer -= game.getTicker().getTickDelay() / 1000.0;
            if (this.shockTimer < 0) this.shockTimer = 0;
            this.velocity.set(0, 0);
            this.input.vector.set(0, 0);
            return;
        }

        this.input.tick();
        tickMovement();
    }

    public void damage(double damage) {
        if (isInvincible() || this.dying)
            return;

        if (this.model != null) {
            SoundBuffer damageBuffer = game.getSounds().entityDamageSounds.get(this.model.getName());
            if (damageBuffer != null)
                game.getSoundManager().playSound(damageBuffer, 1f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.6f);
        }

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

    public void applyKnockback(Vector2dc sourcePos, double force) {
        Vector2d dir = new Vector2d(this.position).sub(sourcePos);

        if (dir.lengthSquared() < 0.001)
            dir.set(1, 0);
        else
            dir.normalize();

        // Просто добавляем вектор к текущей скорости
        this.velocity.add(dir.mul(force));
    }

    protected void tickMovement() {
        double friction = 0.3;
        double acceleration = getAttributeValue(EntityAttributes.ACCELERATION) * friction;

        updateFacing();

        Vector2d inputSpeed = new Vector2d(this.input.vector).normalize()
                .mul(getAttributeValue(EntityAttributes.SPEED) * this.speedMultiplier);
        if (inputSpeed.lengthSquared() > 0.001f) {
            this.velocity.set(
                    this.velocity.x + (inputSpeed.x - this.velocity.x) * acceleration,
                    this.velocity.y + (inputSpeed.y - this.velocity.y) * acceleration
            );

            if (this.model != null && this.model.hasAnimation("go"))
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
        if (this.dying)
            return;

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
