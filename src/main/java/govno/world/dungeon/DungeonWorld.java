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
        spawnBoss();
        spawnExitPortal();
    }

    private void spawnBoss() {
        EnemyType bossType = this.properties.bossType();
        if (bossType == null)
            return;

        Room bossRoom = this.rooms.stream()
                .filter(r -> r.roomType == RoomType.Boss)
                .findFirst()
                .orElse(null);

        if (bossRoom != null) {
            LivingEntity boss = bossType.enemyCreator.apply(bossRoom);

            boss.position.set(bossRoom.centerX, bossRoom.centerY);
            boss.prevPosition.set(boss.position);
            boss.updateAABB();

            bossRoom.entities.add(boss);
        }
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

        for (Room room : this.rooms) {
            if (room.roomType != RoomType.Combat) continue;

            List<Block> floorBlocks = new ArrayList<>();
            for (Block b : room.blocks) {
                if (b.blockType == BlockType.Floor) {
                    floorBlocks.add(b);
                }
            }

            if (floorBlocks.isEmpty()) continue;

            // Берем диапазон из DungeonProperties
            int min = this.properties.minMobs();
            int max = this.properties.maxMobs();

            // Вычисляем случайное число мобов в заданном диапазоне
            int enemiesToSpawn = min + random.nextInt(Math.max(1, max - min + 1));

            Map<EnemyType, Integer> spawnedCounts = new HashMap<>();

            for (int i = 0; i < enemiesToSpawn && !floorBlocks.isEmpty(); i++) {
                // Собираем только тех врагов, чей лимит в комнате еще не достигнут
                Map<EnemyType, SpawnProperty> availableTypes = new HashMap<>();
                double currentTotalPopularity = 0;

                for (Map.Entry<EnemyType, SpawnProperty> entry : spawnProps.entrySet()) {
                    int currentCount = spawnedCounts.getOrDefault(entry.getKey(), 0);
                    if (currentCount < entry.getValue().maxCount()) {
                        availableTypes.put(entry.getKey(), entry.getValue());
                        currentTotalPopularity += entry.getValue().popularity();
                    }
                }

                if (availableTypes.isEmpty() || currentTotalPopularity <= 0) {
                    break;
                }

                EnemyType selectedType = pickWeightedEnemy(availableTypes, currentTotalPopularity, random);
                if (selectedType == null) break;

                Block spawnBlock = floorBlocks.remove(random.nextInt(floorBlocks.size()));
                LivingEntity enemy = selectedType.enemyCreator.apply(room);

                enemy.position.set(spawnBlock.x + 0.5, spawnBlock.y + 0.5);
                enemy.prevPosition.set(enemy.position);
                enemy.updateAABB();

                room.entities.add(enemy);

                int currentCount = spawnedCounts.getOrDefault(selectedType, 0);
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

        return props.keySet().iterator().next();
    }
}