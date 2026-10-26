package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;
import java.util.Random;

public class SlimeInput extends Input {
    private final LivingEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private final Vector2d wanderDir = new Vector2d();
    private int actionTimer = 0;
    private boolean isIdle = false;

    public SlimeInput(LivingEntity owner, Room room) {
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

        if (playerInSameRoom) {
            // РЕЖИМ ПОГОНИ
            Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);

            if (toPlayer.lengthSquared() > 0.01) {
                this.vector.set(toPlayer.normalize());
            } else {
                this.vector.set(0, 0);
            }
            return; // Выходим, чтобы не выполнять логику блуждания
        }

        // РЕЖИМ БЛУЖДАНИЯ
        if (this.actionTimer <= 0) {
            pickNewAction();
        }

        this.actionTimer--;

        if (this.isIdle) {
            this.vector.set(0, 0);
            return;
        }

        // Проверяем, не выходит ли следующий шаг за пределы комнаты
        double nextX = owner.position.x + this.wanderDir.x;
        double nextY = owner.position.y + this.wanderDir.y;

        // Отступы: 1 блок от левого/нижнего края и 2 блока от правого/верхнего
        // (учитывая, что сам слизень занимает 1x1 блок)
        double safeMinX = roomBounds.getMinX() + 1.0;
        double safeMaxX = roomBounds.getMaxX() - 2.0;
        double safeMinY = roomBounds.getMinY() + 1.0;
        double safeMaxY = roomBounds.getMaxY() - 2.0;

        // Если следующий шаг ведет в стену
        if (nextX < safeMinX || nextX > safeMaxX || nextY < safeMinY || nextY > safeMaxY) {
            // Упираемся в стену — останавливаемся и отдыхаем
            this.isIdle = true;
            this.vector.set(0, 0);
            this.actionTimer = 20 + random.nextInt(30); // Стоим 1 - 2.5 секунды
        } else {
            this.vector.set(this.wanderDir);
        }
    }

    private void pickNewAction() {
        // 40% шанс, что враг решит просто постоять
        if (random.nextFloat() < 0.4f) {
            this.isIdle = true;
            this.vector.set(0, 0);
            this.actionTimer = 40 + random.nextInt(60); // Стоим 2 - 5 секунд
        } else {
            // 60% шанс, что враг пойдет гулять
            this.isIdle = false;
            int dir = random.nextInt(4);
            switch (dir) {
                case 0 -> this.wanderDir.set(0, 1);
                case 1 -> this.wanderDir.set(0, -1);
                case 2 -> this.wanderDir.set(1, 0);
                case 3 -> this.wanderDir.set(-1, 0);
            }
            this.actionTimer = 30 + random.nextInt(60); // Идем 1.5 - 4.5 секунд
        }
    }
}