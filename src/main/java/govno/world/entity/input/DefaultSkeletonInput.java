package govno.world.entity.input;

import govno.utils.AABB;
import govno.world.entity.EnemyAttributes;
import govno.world.entity.LivingEntity;
import govno.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class DefaultSkeletonInput extends Input {
    private enum State { WANDER, CHASE, RETREAT }

    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.WANDER;
    private int retreatTimer = 0;
    private int attackCooldown = 0;
    private int wanderTimer = 0;
    private final Vector2d wanderDir = new Vector2d();

    private static final double ATTACK_RANGE = 1.0;
    private static final double SAFE_DISTANCE = 3.5;
    private static final int RETREAT_DURATION = 40; // 2 сек
    private static final int ATTACK_COOLDOWN = 30;  // 1.5 сек

    public DefaultSkeletonInput(LivingEntity owner, Room room) {
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
            wanderTimer = 0;
        } else if (playerInSameRoom && state == State.WANDER) {
            state = State.CHASE;
        }

        if (attackCooldown > 0) attackCooldown--;

        switch (state) {
            case WANDER -> tickWander();
            case CHASE -> tickChase();
            case RETREAT -> tickRetreat();
        }
    }

    public void onHit() {
        if (state != State.RETREAT) {
            state = State.RETREAT;
            retreatTimer = RETREAT_DURATION + 10;
        } else {
            retreatTimer = Math.max(retreatTimer, 20);
        }
    }

    private void tickWander() {
        if (wanderTimer-- <= 0) {
            int dir = random.nextInt(4);
            switch (dir) {
                case 0 -> wanderDir.set(0, 1);
                case 1 -> wanderDir.set(0, -1);
                case 2 -> wanderDir.set(1, 0);
                case 3 -> wanderDir.set(-1, 0);
            }
            wanderTimer = 30 + random.nextInt(60);
        }

        if (!isInsideRoomSafe(wanderDir)) {
            vector.set(0, 0);
            wanderTimer = 0;
        } else {
            vector.set(wanderDir);
        }
    }

    private void tickChase() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        double distance = toPlayer.length();

        if (distance <= ATTACK_RANGE) {
            vector.set(0, 0);
            if (attackCooldown <= 0) {
                // Наносим урон и переходим в отступление
                game.getGameSession().player.damage(owner.getAttributeValue(EnemyAttributes.DAMAGE));
                game.getGameSession().player.applyKnockback(owner.position, 0.3);
                attackCooldown = ATTACK_COOLDOWN;

                if (owner.model != null && owner.model.hasAnimation("attack"))
                    owner.model.play("attack", true);

                state = State.RETREAT;
                retreatTimer = RETREAT_DURATION;
            }
        } else {
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                if (isInsideRoomSafe(toPlayer)) vector.set(toPlayer);
                else vector.set(0, 0);
            }
        }
    }

    private void tickRetreat() {
        retreatTimer--;
        Vector2d away = new Vector2d(owner.position).sub(game.getGameSession().player.position);
        double distance = away.length();

        if (distance >= SAFE_DISTANCE || retreatTimer <= 0) {
            state = State.CHASE;
            return;
        }

        if (away.lengthSquared() > 0.01) {
            away.normalize();
            if (isInsideRoomSafe(away)) {
                vector.set(away);
            } else {
                // Если прижали к стене, пытаемся отойти вбок
                Vector2d sideStep = new Vector2d(-away.y, away.x);
                if (isInsideRoomSafe(sideStep)) vector.set(sideStep);
                else vector.set(0, 0);
            }
        }
    }

    private boolean isInsideRoomSafe(Vector2d dir) {
        double padding = 0.8;
        double nextX = owner.position.x + dir.x;
        double nextY = owner.position.y + dir.y;
        return nextX > roomBounds.getMinX() + padding && nextX < roomBounds.getMaxX() - padding &&
                nextY > roomBounds.getMinY() + padding && nextY < roomBounds.getMaxY() - padding;
    }
}