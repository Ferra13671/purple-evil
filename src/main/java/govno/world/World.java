package govno.world;

import govno.GameInstance;
import govno.utils.AABB;
import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.entity.SlimeEntity;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        spawnEnemies();
    }

    private void spawnEnemies() {
        Random random = new Random();

        // Начинаем с индекса 1, чтобы не спавнить слизней в стартовой комнате игрока
        for (int i = 1; i < rooms.size(); i++) {
            Room room = rooms.get(i);

            // 1. Пропускаем туннели по флагу
            if (room.isCorridor) continue;

            // Собираем все проходимые блоки (пол и выходы) в комнате
            List<Block> floorBlocks = new ArrayList<>();
            for (Block b : room.blocks) {
                if (b.blockType == BlockType.Floor) {
                    floorBlocks.add(b);
                }
            }

            if (floorBlocks.isEmpty()) continue;

            // Спавним от 1 до 3 слизней на комнату
            int slimeCount = 1 + random.nextInt(3);

            // 2. Защита: нельзя заспавнить больше слизней, чем есть свободных блоков пола
            slimeCount = Math.min(slimeCount, floorBlocks.size());

            for (int j = 0; j < slimeCount; j++) {
                Block spawnBlock = floorBlocks.remove(random.nextInt(floorBlocks.size()));

                SlimeEntity slime = new SlimeEntity(
                        new Vector2d(0, 0),
                        null,
                        room
                );

                slime.position.set(spawnBlock.x + 0.5, spawnBlock.y + 0.5);
                slime.prevPosition.set(slime.position);
                slime.updateAABB();

                room.entities.add(slime);
            }
        }
    }

    public void removeEntity(Entity entity) {
        for (Room room : rooms)
            room.entities.remove(entity);
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

                    // Используем новый метод, который сам выбирает кратчайший путь
                    Vector2d out = e1.worldAABB.getPenetrationVector(e2.worldAABB);

                    if (out != null) {
                        e1.onEntityCollision(e2, out);
                        e1.updateAABB();

                        e2.onEntityCollision(e1, out.negate());
                        e2.updateAABB();
                    }
                }
            }
        }
    }
}