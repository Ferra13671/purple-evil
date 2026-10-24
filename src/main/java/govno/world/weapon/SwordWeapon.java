package govno.world.weapon;

import govno.world.attribute.Attributes;
import govno.world.entity.Entity;
import govno.world.entity.LivingEntity;
import govno.world.room.Room;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SwordWeapon extends Weapon {

    private final double damage;
    private final double attackRange;
    private final double attackArc;

    private long swingStartTime = 0;
    private final long swingDurationMs = 250;
    private boolean hasHit = false;

    public SwordWeapon() {
        super("sword", 1.8f, new Attributes(new HashMap<>()), new HashMap<>());
        this.damage = 20;
        this.attackRange = 2.0;
        this.attackArc = 120;
    }

    public boolean isSwinging() {
        return System.currentTimeMillis() - swingStartTime < swingDurationMs;
    }

    public float getSwingProgress() {
        if (!isSwinging()) return 1f;
        return (System.currentTimeMillis() - swingStartTime) / (float) swingDurationMs;
    }

    @Override
    public void startUse(LivingEntity entity) {
        if (isSwinging()) return; // Защита от спама

        this.swingStartTime = System.currentTimeMillis();
        this.hasHit = false;

        game.getSoundManager().playRawSound(game.getSounds().SWING, 0.5f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
    }

    @Override
    public void tick(LivingEntity entity) {
        if (isSwinging() && !this.hasHit) {
            float progress = getSwingProgress();

            if (progress >= 0.4f) {
                this.hasHit = true;
                performHit(entity);
            }
        }
    }

    private void performHit(LivingEntity entity) {
        Room room = game.getWorld().currentRoom;

        double handX = entity.position.x;
        double handY = entity.position.y + 0.5;

        for (Entity target : room.entities) {
            if (target == entity || !(target instanceof LivingEntity livingTarget) || livingTarget.getHealth() <= 0) continue;

            double closestX = Math.clamp(handX, livingTarget.worldAABB.getMinX(), livingTarget.worldAABB.getMaxX());
            double closestY = Math.clamp(handY, livingTarget.worldAABB.getMinY(), livingTarget.worldAABB.getMaxY());
            double distance = Math.sqrt(Math.pow(handX - closestX, 2) + Math.pow(handY - closestY, 2));

            if (distance <= this.attackRange) {
                double dx = livingTarget.position.x - handX;
                double dy = livingTarget.position.y - handY;
                double targetAngle = Math.toDegrees(Math.atan2(dy, dx));

                double angleDiff = Math.abs(entity.yaw - targetAngle) % 360;
                if (angleDiff > 180) angleDiff = 360 - angleDiff;

                if (angleDiff <= attackArc / 2) {
                    game.getSoundManager().playRawSound(game.getSounds().SWING_HIT, 0.3f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
                    livingTarget.damage(damage);

                    Vector2d knockback = new Vector2d(dx, dy).normalize().mul(0.3);
                    livingTarget.velocity.add(knockback);
                }
            }
        }
    }

    public float getRenderAngle(LivingEntity entity) {
        float baseAngle = (float) entity.getRenderYaw();
        if (!isSwinging()) return baseAngle;

        float progress = getSwingProgress();
        float easedProgress = progress * progress * (3.0f - 2.0f * progress);

        float startOffset = -60f;
        float endOffset = 60f;
        float currentOffset = startOffset + (endOffset - startOffset) * easedProgress;

        return baseAngle + currentOffset;
    }
}