package stayablox.purpleevil.world.weapon;

import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.Entity;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SwordWeapon extends Weapon {
    private final double attackRange;
    private final double attackArc;

    private boolean hasHit = false;

    public SwordWeapon() {
        super("sword", 1.3f, new Attributes(new HashMap<>(){{
            put(WeaponAttributes.DAMAGE, 20d);
            put(WeaponAttributes.ATTACK_SPEED, 1d);
            put(WeaponAttributes.COOLDOWN_MS, 500d);
            put(WeaponAttributes.POISONING, 0d);
        }}), new HashMap<>());
        this.attackRange = 2.0;
        this.attackArc = 120;
    }

    public boolean isSwinging() {
        return !canUse();
    }

    public float getSwingProgress() {
        return getCooldownProgress();
    }

    @Override
    protected void attack(LivingEntity entity) {
        super.attack(entity);

        this.hasHit = false;
        game.getSoundManager().playSound(game.getSounds().SWING, 0.5f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
    }

    @Override
    public void tick(LivingEntity entity) {
        super.tick(entity); // Вызываем базовый tick (для автоатаки)

        // Логика нанесения урона в середине взмаха
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
                    game.getSoundManager().playSound(game.getSounds().SWING_HIT, 0.3f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
                    livingTarget.damage(getAttributeValue(WeaponAttributes.DAMAGE));

                    double poison = getAttributeValue(WeaponAttributes.POISONING);
                    if (poison > 0)
                        livingTarget.applyPoison(poison, 60);

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