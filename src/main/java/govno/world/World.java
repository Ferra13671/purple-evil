package govno.world;

import govno.GameInstance;
import govno.utils.AABB;
import govno.world.block.Block;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;

public class World implements GameInstance {
    public static final double MAX_VELOCITY_STEP = 0.25;
    // Радиус, в котором комнаты остаются активными (тикаются)
    public static final double ACTIVE_RADIUS = 60.0;

    public final Camera camera = new Camera();
    public final Player player = new Player();
    public List<Room> rooms = new DungeonGenerator(
            game.getRoomTemplates(),
            System.nanoTime()
    ).generate();
    public Room currentRoom = this.rooms.getFirst();

    public World() {
        this.currentRoom.entities.add(this.player);
    }

    public void tick() {
        // 1. Тикаем логику игрока (ввод, поворот и т.д.)
        this.player.tick();

        // 2. Определяем зону активности вокруг игрока
        AABB activeArea = new AABB(
                player.position.x - ACTIVE_RADIUS, player.position.y - ACTIVE_RADIUS,
                player.position.x + ACTIVE_RADIUS, player.position.y + ACTIVE_RADIUS
        );

        // 3. Находим все комнаты в этой зоне
        List<Room> activeRooms = new ArrayList<>();
        for (Room room : this.rooms) {
            if (room.intersects(activeArea)) {
                activeRooms.add(room);
            }
        }

        // 4. Тикаем сущностей во ВСЕХ активных комнатах (ИИ врагов)
        for (Room room : activeRooms) {
            room.tick();
        }

        // 5. Обновляем currentRoom (для рендера и камеры)
        for (Room room : activeRooms) {
            if (room.intersects(this.player.worldAABB) && room != this.currentRoom) {
                this.currentRoom.entities.remove(this.player);
                this.currentRoom = room;
                this.currentRoom.entities.add(this.player);
                break; // Нам нужен только один currentRoom
            }
        }

        // 6. Собираем всех сущностей и все блоки из активных комнат для физики
        List<Entity> activeEntities = new ArrayList<>();
        List<Block> activeBlocks = new ArrayList<>();
        for (Room room : activeRooms) {
            activeEntities.addAll(room.entities);
            activeBlocks.addAll(room.blocks);
        }

        // 7. Применяем физику ко всем активным сущностям
        tickPhysics(activeEntities, activeBlocks);

        // 8. Обновляем камеру
        this.camera.tick();
    }

    // Изменили сигнатуру метода: теперь он принимает списки, а не одну комнату
    public void tickPhysics(List<Entity> entities, List<Block> blocks) {
        if (entities.isEmpty()) return;

        double maxSpeed = 0;
        for (Entity e : entities)
            maxSpeed = Math.max(maxSpeed, e.velocity.length());

        int steps = Math.max((int) Math.ceil(maxSpeed / MAX_VELOCITY_STEP), 1);
        double stepDelta = 1.0 / steps;

        for (int step = 0; step < steps; step++) {
            for (Entity e : entities) {
                e.position.y += e.velocity.y * stepDelta;
                e.updateAABB();
            }
            for (Entity e : entities) {
                for (Block block : blocks) {
                    if (block.collisionBox == null) continue;
                    Vector2dc out = e.worldAABB.getPenetrationVector(block.collisionBox, false);
                    if (out != null) { e.onWorldCollision(out); e.updateAABB(); }
                }
            }

            for (Entity e : entities) {
                e.position.x += e.velocity.x * stepDelta;
                e.updateAABB();
            }
            for (Entity e : entities) {
                for (Block block : blocks) {
                    if (block.collisionBox == null) continue;
                    Vector2dc out = e.worldAABB.getPenetrationVector(block.collisionBox, true);
                    if (out != null) { e.onWorldCollision(out); e.updateAABB(); }
                }
            }

            for (int i = 0; i < entities.size(); i++) {
                for (int j = i + 1; j < entities.size(); j++) {
                    Entity e1 = entities.get(i), e2 = entities.get(j);

                    Vector2d outX = e1.worldAABB.getPenetrationVector(e2.worldAABB, true);
                    if (outX != null) {
                        e1.onEntityCollision(e2, outX); e1.updateAABB();
                        e2.onEntityCollision(e1, outX.negate()); e2.updateAABB();
                    }

                    Vector2d outY = e1.worldAABB.getPenetrationVector(e2.worldAABB, false);
                    if (outY != null) {
                        e1.onEntityCollision(e2, outY); e1.updateAABB();
                        e2.onEntityCollision(e1, outY.negate()); e2.updateAABB();
                    }
                }
            }
        }
    }
}