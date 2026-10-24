package govno.world.dungeon;

import govno.world.dungeon.spawn.EnemyType;
import govno.world.dungeon.spawn.SpawnProperty;

import java.util.Map;

public record DungeonProperties(int tunnelLength, int maxRooms, double branchChance, Map<EnemyType, SpawnProperty> spawnProperties, int maxMobsInRoom) {
}
