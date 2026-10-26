package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.EnemyAttributes;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class KnightSkeletonInput extends Input {
    private enum State { WANDER, CHASE }

    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.WANDER;
    private int attackCooldown = 0;
    private int wanderTimer = 0;
    private final Vector2d wanderDir = new Vector2d();

    private static final double ATTACK_RANGE = 1.2;  // Длинный меч, большая дальность
    private static final int ATTACK_COOLDOWN = 60;   // 3 секунды (медленный замах)

    public KnightSkeletonInput(LivingEntity owner, Room room) {
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

        if (state == State.WANDER) {
            tickWander();
        } else {
            tickChase();
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
            wanderTimer = 50 + random.nextInt(50);
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
            // Если игрок в зоне удара — бьем
            vector.set(0, 0); // Останавливаемся для удара
            
            if (attackCooldown <= 0) {
                // Наносим тяжелый урон и откидываем
                game.getGameSession().player.damage(owner.getAttributeValue(EnemyAttributes.DAMAGE));
                game.getGameSession().player.applyKnockback(owner.position, 1.0); // Сильный отброс
                attackCooldown = ATTACK_COOLDOWN;

                if (owner.model != null && owner.model.hasAnimation("attack"))
                    owner.model.play("attack", true);
            }
        } else {
            // Идем к игроку. Не останавливается, даже если его бьют.
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                if (isInsideRoomSafe(toPlayer)) {
                    vector.set(toPlayer);
                } else {
                    // Если игрок за стеной, пытаемся обойти
                    Vector2d sideStep = new Vector2d(-toPlayer.y, toPlayer.x);
                    if (isInsideRoomSafe(sideStep)) vector.set(sideStep);
                    else vector.set(0, 0);
                }
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