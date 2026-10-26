package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class BatInput extends Input {
    private enum State {
        WANDER,   // Блуждание
        TO_WALL,  // Летит к ближайшей стене
        WINDUP,   // Замах
        DASH,     // Таран
        STUN      // Очень короткая остановка
    }

    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.WANDER;
    private int timer = 0;

    private final Vector2d wanderDir = new Vector2d();
    private final Vector2d dashDir = new Vector2d();

    public BatInput(LivingEntity owner, Room room) {
        this.owner = owner;
        this.roomBounds = room.getBounds();
    }

    @Override
    public void tick() {
        if (game.getWorld() == null || game.getWorld().currentRoom == null) {
            this.vector.set(0, 0);
            return;
        }

        boolean playerInSameRoom = game.getWorld().currentRoom.entities.contains(owner);

        if (!playerInSameRoom && state != State.WANDER) {
            state = State.WANDER;
            timer = 0;
        } else if (playerInSameRoom && state == State.WANDER) {
            state = State.TO_WALL;
            timer = 60;
        }

        switch (state) {
            case WANDER -> tickWander();
            case TO_WALL -> tickToWall();
            case WINDUP -> tickWindup();
            case DASH -> tickDash();
            case STUN -> tickStun();
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

        if (minDist < 1.0 || owner.velocity.lengthSquared() < 0.001 || timer-- <= 0) {
            state = State.WINDUP;
            timer = 10;
            vector.set(0, 0);
            return;
        }

        if (minDist == distLeft) vector.set(-1, 0);
        else if (minDist == distRight) vector.set(1, 0);
        else if (minDist == distBottom) vector.set(0, -1);
        else if (minDist == distTop) vector.set(0, 1);
    }

    private void tickWindup() {
        vector.set(0, 0);

        if (timer-- <= 0) {
            dashDir.set(game.getGameSession().player.position).sub(owner.position);
            if (dashDir.lengthSquared() > 0.01) {
                dashDir.normalize();
                state = State.DASH;
                timer = 30;
            } else {
                state = State.STUN;
                timer = 3; // Короткая пауза, если игрок прямо в центре мыши
            }
        }
    }

    private void tickDash() {
        vector.set(dashDir);

        // Если врезались в стену (скорость упала)
        if (timer < 25 && owner.velocity.lengthSquared() < 0.001) {
            state = State.STUN;
            timer = 3;
            vector.set(0, 0);
            return;
        }

        // Если время рывка вышло
        if (timer-- <= 0) {
            state = State.STUN;
            timer = 3;
            vector.set(0, 0);
        }
    }

    private void tickStun() {
        vector.set(0, 0);
        if (timer-- <= 0) {
            state = State.TO_WALL;
            timer = 60;
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