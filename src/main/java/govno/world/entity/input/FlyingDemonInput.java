package govno.world.entity.input;

import govno.utils.AABB;
import govno.world.entity.EnemyAttributes;
import govno.world.entity.LivingEntity;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.Random;

public class FlyingDemonInput extends Input {
    private enum State {
        WANDER,
        TO_WALL,
        WINDUP,
        FLY_TO_PLAYER,
        ATTACK,
        RETREAT
    }

    // ===== Конфигурация =====
    private static final double ATTACK_RANGE = 1.3;
    private static final double ATTACK_HIT_RANGE = 1.8;
    private static final int    ATTACK_DURATION = 20;
    private static final int    ATTACK_COOLDOWN = 60;
    private static final int    RETREAT_DURATION = 30;
    private static final double RETREAT_SAFE_DISTANCE = 4.0;
    private static final float  FLY_SPEED_MULTIPLIER = 1.8f;
    private static final double KNOCKBACK_FORCE = 0.6;

    // ===== Замах + коррекция рывка =====
    private static final int    WINDUP_DURATION = 12;
    private static final double MAX_TURN_PER_TICK_DEG = 2.0;

    // Порог «стена впереди, а не сбоку». out — вектор выталкивания из стены.
    // Если -out смотрит туда, куда мы летим (dot(out, chargeDir) < -0.3) — это
    // лобовое/окололобовое столкновение → перезапуск. Если dot ≈ 0 — скользим.
    private static final double WALL_HIT_DOT_THRESHOLD = -0.3;

    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.WANDER;
    private int timer = 0;
    private int attackCooldown = 0;
    private boolean hasHit = false;

    private final Vector2d wanderDir = new Vector2d();
    private final Vector2d chargeDir = new Vector2d();

    public FlyingDemonInput(LivingEntity owner, Room room) {
        this.owner = owner;
        this.roomBounds = room.getBounds();
    }

    @Override
    public void tick() {
        if (game.getWorld() == null || game.getWorld().currentRoom == null) {
            vector.set(0, 0);
            return;
        }

        boolean playerInSameRoom = game.getWorld().currentRoom.entities.contains(owner);

        if (!playerInSameRoom && state != State.WANDER) {
            state = State.WANDER;
            timer = 0;
            hasHit = false;
            owner.setSpeedMultiplier(1f);
        } else if (playerInSameRoom && state == State.WANDER) {
            state = State.TO_WALL;
            timer = 60;
            owner.setSpeedMultiplier(1f);
        }

        if (attackCooldown > 0) attackCooldown--;

        switch (state) {
            case WANDER -> tickWander();
            case TO_WALL -> tickToWall();
            case WINDUP -> tickWindup();
            case FLY_TO_PLAYER -> tickFlyToPlayer();
            case ATTACK -> tickAttack();
            case RETREAT -> tickRetreat();
        }
    }

    /**
     * Вызывается из FlyingDemonEntity.onWorldCollision после разрешения коллизии.
     * Если мы в рывке и стена реально впереди — перезапускаем замах.
     */
    public void onWallHit(Vector2dc out) {
        if (state != State.FLY_TO_PLAYER)
            return;

        double lenSq = out.x() * out.x() + out.y() * out.y();
        if (lenSq < 1e-8)
            return;

        double normDot = (out.x() * chargeDir.x + out.y() * chargeDir.y) / Math.sqrt(lenSq);

        // out смотрит ПРОТИВ chargeDir → стена лобовая
        if (normDot < WALL_HIT_DOT_THRESHOLD) {
            startWindup();
        }
    }

    private void tickWander() {
        if (timer-- <= 0) {
            int dir = random.nextInt(8);
            double angle = dir * (Math.PI / 4);
            wanderDir.set(Math.cos(angle), Math.sin(angle));
            timer = 10 + random.nextInt(15);
        }

        if (!isInsideRoomSafe(wanderDir)) {
            vector.set(0, 0);
            timer = 0;
        } else {
            vector.set(wanderDir);
        }
    }

    private void tickToWall() {
        double distLeft = owner.position.x - roomBounds.getMinX();
        double distRight = roomBounds.getMaxX() - owner.position.x;
        double distBottom = owner.position.y - roomBounds.getMinY();
        double distTop = roomBounds.getMaxY() - owner.position.y;
        double minDist = Math.min(Math.min(distLeft, distRight), Math.min(distBottom, distTop));

        if (minDist < 1.0 || timer-- <= 0) {
            startWindup();
            return;
        }

        if (minDist == distLeft) vector.set(-1, 0);
        else if (minDist == distRight) vector.set(1, 0);
        else if (minDist == distBottom) vector.set(0, -1);
        else if (minDist == distTop) vector.set(0, 1);
    }

    private void startWindup() {
        state = State.WINDUP;
        timer = WINDUP_DURATION;
        vector.set(0, 0);
        owner.setSpeedMultiplier(1f);
        owner.velocity.set(0, 0);

        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        if (toPlayer.lengthSquared() > 0.01) {
            chargeDir.set(toPlayer).normalize();
        } else {
            double angle = random.nextDouble() * Math.PI * 2;
            chargeDir.set(Math.cos(angle), Math.sin(angle));
        }

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickWindup() {
        vector.set(0, 0);

        if (timer-- <= 0) {
            state = State.FLY_TO_PLAYER;
            owner.setSpeedMultiplier(FLY_SPEED_MULTIPLIER);
            vector.set(chargeDir);
        }
    }

    private void tickFlyToPlayer() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        double distance = toPlayer.length();

        if (distance <= ATTACK_RANGE) {
            if (attackCooldown > 0) {
                state = State.TO_WALL;
                timer = 30;
                owner.setSpeedMultiplier(1f);
                return;
            }

            state = State.ATTACK;
            timer = ATTACK_DURATION;
            hasHit = false;
            owner.setSpeedMultiplier(1f);
            vector.set(0, 0);
            owner.velocity.set(0, 0);
            return;
        }

        // Лёгкая коррекция: поворот не более чем на MAX_TURN_PER_TICK_DEG
        if (toPlayer.lengthSquared() > 0.01) {
            Vector2d normToPlayer = new Vector2d(toPlayer).normalize();

            double currentAngle = Math.atan2(chargeDir.y, chargeDir.x);
            double targetAngle = Math.atan2(normToPlayer.y, normToPlayer.x);

            double delta = targetAngle - currentAngle;
            if (delta > Math.PI) delta -= 2 * Math.PI;
            else if (delta < -Math.PI) delta += 2 * Math.PI;

            double maxRad = Math.toRadians(MAX_TURN_PER_TICK_DEG);
            double clamped = Math.clamp(delta, -maxRad, maxRad);

            double newAngle = currentAngle + clamped;
            chargeDir.set(Math.cos(newAngle), Math.sin(newAngle));
        }
        vector.set(chargeDir);
    }

    private void tickAttack() {
        vector.set(0, 0);

        if (!hasHit) {
            double distToPlayer = new Vector2d(game.getGameSession().player.position)
                    .sub(owner.position).length();
            if (distToPlayer <= ATTACK_HIT_RANGE) {
                hasHit = true;
                game.getGameSession().player.damage(owner.getAttributeValue(EnemyAttributes.DAMAGE));
                game.getGameSession().player.applyKnockback(owner.position, KNOCKBACK_FORCE);
                attackCooldown = ATTACK_COOLDOWN;
            }
        }

        if (timer-- <= 0) {
            state = State.RETREAT;
            timer = RETREAT_DURATION;
        }
    }

    private void tickRetreat() {
        Vector2d away = new Vector2d(owner.position).sub(game.getGameSession().player.position);
        double distance = away.length();

        if (distance >= RETREAT_SAFE_DISTANCE || timer-- <= 0) {
            state = State.TO_WALL;
            timer = 30;
            owner.setSpeedMultiplier(1f);
            return;
        }

        if (away.lengthSquared() > 0.01) {
            away.normalize();
            if (isInsideRoomSafe(away)) {
                vector.set(away);
            } else {
                Vector2d sideStep = new Vector2d(-away.y, away.x);
                if (isInsideRoomSafe(sideStep)) vector.set(sideStep);
                else vector.set(0, 0);
            }
        }
    }

    private boolean isInsideRoomSafe(Vector2d dir) {
        double nextX = owner.position.x + dir.x;
        double nextY = owner.position.y + dir.y;
        double padding = 0.5;

        return nextX > roomBounds.getMinX() + padding &&
                nextX < roomBounds.getMaxX() - padding &&
                nextY > roomBounds.getMinY() + padding &&
                nextY < roomBounds.getMaxY() - padding;
    }
}