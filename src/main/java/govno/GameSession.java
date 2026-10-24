package govno;

import govno.model.EntityModel;
import govno.world.LobbyWorld;
import govno.world.dungeon.DungeonProperties;
import govno.world.dungeon.DungeonWorld;
import govno.world.dungeon.spawn.EnemyType;
import govno.world.dungeon.spawn.SpawnProperty;
import govno.world.entity.Player;
import govno.world.weapon.MagicStaffWeapon;
import govno.world.weapon.SwordWeapon;

import java.util.HashMap;
import java.util.Map;

public class GameSession implements GameInstance {
    private final Player knight = new Player(
            new EntityModel(game.getEntityModelTemplates().templates.get("knight")),
            new MagicStaffWeapon()
    );
    private final Player mage = new Player(
            new EntityModel(game.getEntityModelTemplates().templates.get("mage")),
            new SwordWeapon()
    );

    public Player player = this.mage;
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
        this.currentLevel++;

        this.player = this.currentLevel % 2 == 0 ? this.mage : this.knight;
        this.player.setHealth(this.player.getAttributeValue("max_health"));
        this.player.setInvincibilityTimer(0);

        startLobby();
    }

    private DungeonProperties createPropertiesForLevel(int level) {
        int rooms = 5 + level * 2;
        int maxMobs = 2 + level;
        int tunnelLength = 10;

        Map<EnemyType, SpawnProperty> spawns = new HashMap<>();
        spawns.put(EnemyType.Slime, new SpawnProperty(3 + level, 1.0 + level * 0.2));
        spawns.put(EnemyType.Bat, new SpawnProperty(3 + level, 1.0 + level * 0.2));

        return new DungeonProperties(tunnelLength, rooms, 0.05, spawns, maxMobs);
    }
}