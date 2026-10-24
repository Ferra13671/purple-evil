package govno;

import govno.world.LobbyWorld;
import govno.world.dungeon.DungeonProperties;
import govno.world.dungeon.DungeonWorld;
import govno.world.dungeon.spawn.EnemyType;
import govno.world.dungeon.spawn.SpawnProperty;
import govno.world.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameSession implements GameInstance {
    public final Player player = new Player();
    public int currentLevel = 1;

    public void startGame() {
        startLobby();
    }

    public void startLobby() {
        game.setWorld(new LobbyWorld());
    }

    public void startDungeon() {
        DungeonProperties props = createPropertiesForLevel(currentLevel);
        game.setWorld(new DungeonWorld(props));
    }

    public void completeDungeon() {
        currentLevel++;
        startLobby();
    }

    private DungeonProperties createPropertiesForLevel(int level) {
        int rooms = 5 + level * 2;
        int maxMobs = 2 + level;
        int tunnelLength = 10;

        Map<EnemyType, SpawnProperty> spawns = new HashMap<>();
        spawns.put(EnemyType.Slime, new SpawnProperty(3 + level, 1.0 + level * 0.2));

        return new DungeonProperties(tunnelLength, rooms, 0.05, spawns, maxMobs);
    }
}