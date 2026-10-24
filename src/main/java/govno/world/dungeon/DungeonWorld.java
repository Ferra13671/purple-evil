package govno.world.dungeon;

import govno.world.LobbyWorld;
import govno.world.World;
import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.dungeon.spawn.EnemyType;
import govno.world.dungeon.spawn.SpawnProperty;
import govno.world.entity.LivingEntity;
import govno.world.entity.PortalEntity;
import govno.world.room.Room;
import govno.world.room.RoomType;
import org.joml.Vector2d;

import java.util.*;

public class DungeonWorld extends World {

    private final DungeonProperties properties;

    public DungeonWorld(DungeonProperties properties) {
        super(new DungeonGenerator(game.getRoomTemplates(), properties, System.nanoTime()).generate());
        this.properties = properties;

        spawnEnemies();
        spawnExitPortal();
    }

    private void spawnExitPortal() {
        Room exitRoom = this.rooms.stream()
                .filter(r -> r.roomType == RoomType.Exit)
                .findFirst()
                .orElse(null);

        if (exitRoom != null) {
            Block portalBlock = LobbyWorld.findPortalBlock(exitRoom.blocks);
            if (portalBlock != null) {
                PortalEntity portal = new PortalEntity(
                        new Vector2d(
                                portalBlock.x + 0.5,
                                portalBlock.y + 0.5
                        ),
                        () -> game.getGameSession().completeDungeon()
                );
                exitRoom.entities.add(portal);
            }
        }
    }

    private void spawnEnemies() {
        Random random = new Random();
        Map<EnemyType, SpawnProperty> spawnProps = this.properties.spawnProperties();

        if (spawnProps == null || spawnProps.isEmpty()) return;

        // Вычисляем общий вес популярности для взвешенного рандома
        double totalPopularity = spawnProps.values().stream()
                .mapToDouble(SpawnProperty::popularity)
                .sum();

        for (Room room : this.rooms) {
            if (room.roomType != RoomType.Combat) continue;

            List<Block> floorBlocks = new ArrayList<>();
            for (Block b : room.blocks) {
                if (b.blockType == BlockType.Floor) {
                    floorBlocks.add(b);
                }
            }

            if (floorBlocks.isEmpty()) continue;

            // Спавним от 1 до 4 мобов на комнату (можно тоже вынести в проперти, если нужно)
            int enemiesToSpawn = 1 + random.nextInt(this.properties.maxMobsInRoom());
            Map<EnemyType, Integer> spawnedCounts = new HashMap<>();

            for (int i = 0; i < enemiesToSpawn && !floorBlocks.isEmpty(); i++) {
                // Выбираем тип врага на основе веса (popularity)
                EnemyType selectedType = pickWeightedEnemy(spawnProps, totalPopularity, random);
                if (selectedType == null) continue;

                SpawnProperty prop = spawnProps.get(selectedType);
                int currentCount = spawnedCounts.getOrDefault(selectedType, 0);

                // Если превысили лимит для этого типа в комнате — пропускаем итерацию
                if (currentCount >= prop.maxCount()) {
                    continue;
                }

                // Спавним
                Block spawnBlock = floorBlocks.remove(random.nextInt(floorBlocks.size()));
                LivingEntity enemy = selectedType.enemyCreator.apply(room);

                enemy.position.set(spawnBlock.x + 0.5, spawnBlock.y + 0.5);
                enemy.prevPosition.set(enemy.position);
                enemy.updateAABB();

                room.entities.add(enemy);
                spawnedCounts.put(selectedType, currentCount + 1);
            }
        }
    }

    private EnemyType pickWeightedEnemy(Map<EnemyType, SpawnProperty> props, double totalPopularity, Random random) {
        if (totalPopularity <= 0) return null;

        double roll = random.nextDouble() * totalPopularity;
        double currentWeight = 0;

        for (Map.Entry<EnemyType, SpawnProperty> entry : props.entrySet()) {
            currentWeight += entry.getValue().popularity();
            if (roll <= currentWeight) {
                return entry.getKey();
            }
        }
        return null;
    }
}