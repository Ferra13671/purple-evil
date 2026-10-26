package stayablox.purpleevil.world.dungeon;

import stayablox.purpleevil.world.dungeon.spawn.EnemyType;
import stayablox.purpleevil.world.dungeon.spawn.SpawnProperty;

import java.util.Map;

public record DungeonProperties(
        int tunnelLength,
        int maxRooms,
        double branchChance,
        Map<EnemyType, SpawnProperty> spawnProperties,
        int minMobs,
        int maxMobs,
        EnemyType bossType
) { }