package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.DemonArrowEntity;
import stayablox.purpleevil.world.entity.EnemyAttributes;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class CrossbowmanDemonInput extends Input {
    private enum State { WANDER, KEEP_DISTANCE }

    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.WANDER;
    private int shootCooldown = 0;
    private int wanderTimer = 0;
    private final Vector2d wanderDir = new Vector2d();

    // ===== Зона комфорта и темп стрельбы =====
    private static final double MIN_DISTANCE = 4.5;      // ближе — отходит
    private static final double MAX_DISTANCE = 9.0;      // дальше — подходит
    private static final int    SHOOT_COOLDOWN_TICKS = 70; // 3.5 сек (перезарядка арбалета)
    private static final double ARROW_SPEED = 0.55;      // быстрее skeleton-orb (0.4)

    public CrossbowmanDemonInput(LivingEntity owner, Room room) {
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
            state = State.KEEP_DISTANCE;
        }

        if (shootCooldown > 0) shootCooldown--;

        switch (state) {
            case WANDER -> tickWander();
            case KEEP_DISTANCE -> tickKeepDistance();
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
            wanderTimer = 40 + random.nextInt(40);
        }

        if (!isInsideRoomSafe(wanderDir)) {
            vector.set(0, 0);
            wanderTimer = 0;
        } else {
            vector.set(wanderDir);
        }
    }

    private void tickKeepDistance() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        double distance = toPlayer.length();

        if (distance < MIN_DISTANCE) {
            // Игрок слишком близко — отходим
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                Vector2d away = new Vector2d(toPlayer).negate();
                if (isInsideRoomSafe(away)) vector.set(away);
                else {
                    // Прижали к стене — уходим вбок
                    Vector2d sideStep = new Vector2d(-away.y, away.x);
                    if (isInsideRoomSafe(sideStep)) vector.set(sideStep);
                    else vector.set(0, 0);
                }
            }
        } else if (distance > MAX_DISTANCE) {
            // Игрок далеко — подходим
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                if (isInsideRoomSafe(toPlayer)) vector.set(toPlayer);
                else vector.set(0, 0);
            }
        } else {
            // Зона комфорта — стоим и стреляем
            vector.set(0, 0);
            if (shootCooldown <= 0) {
                shoot(toPlayer);
            }
        }
    }

    private void shoot(Vector2d toPlayer) {
        this.shootCooldown = SHOOT_COOLDOWN_TICKS;

        if (toPlayer.lengthSquared() > 0.01) {
            toPlayer.normalize();

            Vector2d spawnPos = new Vector2d(owner.position);
            Vector2d velocity = new Vector2d(toPlayer).mul(ARROW_SPEED);

            game.getWorld().currentRoom.entities.add(new DemonArrowEntity(
                    spawnPos,
                    velocity,
                    owner.getAttributeValue(EnemyAttributes.DAMAGE)
            ));

            if (owner.model != null && owner.model.hasAnimation("attack"))
                owner.model.play("attack", true);
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