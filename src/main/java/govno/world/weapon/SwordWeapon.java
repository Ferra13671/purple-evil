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

    // --- СОСТОЯНИЕ ВЗМАХА ---
    private long swingStartTime = 0;
    private final long swingDurationMs = 250; // Взмах длится 250 мс

    public SwordWeapon() {
        super("sword", 1.8f, new Attributes(new HashMap<>()), new HashMap<>());
        this.damage = 35;
        this.attackRange = 2.2;
        this.attackArc = 120;
    }

    public boolean isSwinging() {
        return System.currentTimeMillis() - swingStartTime < swingDurationMs;
    }

    public float getSwingProgress() {
        if (!isSwinging()) return 1f;
        return (System.currentTimeMillis() - swingStartTime) / (float) swingDurationMs;
    }

    /** Вычисляет угол меча с учетом взмаха */
    public float getRenderAngle(LivingEntity entity) {
        float baseAngle = (float) entity.getRenderYaw();
        if (!isSwinging()) return baseAngle;

        float progress = getSwingProgress();

        // Сглаживание (Smoothstep) - делает взмах более резким и естественным
        float easedProgress = progress * progress * (3.0f - 2.0f * progress);

        // Взмах от -90 (слева/сзади) до +90 (справа/спереди)
        float startOffset = -90f;
        float endOffset = 90f;
        float currentOffset = startOffset + (endOffset - startOffset) * easedProgress;

        return baseAngle + currentOffset;
    }

    @Override
    public void startUse(LivingEntity entity) {
        if (isSwinging())
            return; // Защита от спама

        this.swingStartTime = System.currentTimeMillis();

        game.getSoundManager().playRawSound(game.getSounds().SWING, 0.5f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));

        // Логика нанесения урона (остается без изменений)
        Room room = game.getWorld().currentRoom;
        for (Entity target : room.entities) {
            if (target == entity || !(target instanceof LivingEntity livingTarget) || livingTarget.getHealth() <= 0) continue;

            double dx = livingTarget.position.x - entity.position.x;
            double dy = livingTarget.position.y - entity.position.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= attackRange) {
                double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
                double angleDiff = Math.abs(entity.yaw - targetAngle) % 360;
                if (angleDiff > 180) angleDiff = 360 - angleDiff;

                if (angleDiff <= attackArc / 2) {
                    game.getSoundManager().playRawSound(game.getSounds().SWING_HIT, 0.3f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
                    livingTarget.damage(damage);

                    // Откидывание
                    Vector2d knockback = new Vector2d(dx, dy).normalize().mul(0.3);
                    livingTarget.velocity.add(knockback);
                }
            }
        }
    }
}