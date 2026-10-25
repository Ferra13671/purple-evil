package govno;

import govno.model.EntityModel;
import govno.screen.DeathScreen;
import govno.utils.AABB;
import govno.world.LobbyWorld;
import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.dungeon.DungeonProperties;
import govno.world.dungeon.DungeonWorld;
import govno.world.dungeon.spawn.EnemyType;
import govno.world.dungeon.spawn.SpawnProperty;
import govno.world.entity.EnemyEntity;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import govno.world.weapon.MagicStaffWeapon;
import govno.world.weapon.SwordWeapon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GameSession implements GameInstance {
    private final Player knight = new Player(
            new EntityModel(game.getEntityModelTemplates().templates.get("knight")),
            new MagicStaffWeapon()
    );
    private final Player mage = new Player(
            new EntityModel(game.getEntityModelTemplates().templates.get("mage")),
            new SwordWeapon()
    );

    public Player player = this.knight;
    public int currentLevel = 1;
    public int score = 0;

    private Checkpoint checkpoint = null;

    private static final long SWAP_COOLDOWN_MS = 5000;
    private long lastSwapTime = 0;

    public void startGame() {
        this.score = 0;
        startLobby();
    }

    public void startLobby() {
        saveCheckpoint();
        game.setWorld(new LobbyWorld());
    }

    public void startDungeon() {
        DungeonProperties props = createPropertiesForLevel(currentLevel);
        game.setWorld(new DungeonWorld(props));
    }

    public void completeDungeon() {
        this.currentLevel++;
        startLobby();
    }

    public void loadCheckpoint() {
        if (this.checkpoint != null) {
            this.score = checkpoint.score();
            this.currentLevel = checkpoint.level();

            this.knight.revive();
            this.mage.revive();

            this.knight.setHealth(this.checkpoint.knightHealth());
            this.mage.setHealth(this.checkpoint.mageHealth());

            this.knight.getAttributes().loadModifiers(this.checkpoint.knightMods());
            this.mage.getAttributes().loadModifiers(this.checkpoint.mageMods());

            this.player = this.knight;
        }
    }

    private void saveCheckpoint() {
        this.checkpoint = new Checkpoint(
                this.knight.getHealth(), this.mage.getHealth(),
                this.knight.getAttributes().saveModifiers(), this.mage.getAttributes().saveModifiers(),
                this.score, this.currentLevel
        );
    }

    public void tick() {
        if (Keybinds.PLAYER_SWAP.isPressed()) {
            trySwapPlayer();
            Keybinds.PLAYER_SWAP.setPressed(false);
        }

        if (game.getWorld() != null) {
            Room currentRoom = game.getWorld().currentRoom;
            if (currentRoom.roomType.combat) {
                AABB roomBounds = currentRoom.getBounds();
                List<Block> floorBlocks = currentRoom.getBlocks(BlockType.Floor);

                if (!floorBlocks.isEmpty()) {
                    for (Entity entity : currentRoom.entities) {
                        if (entity instanceof EnemyEntity && !entity.removed) {
                            if (!roomBounds.contains(entity.position.x, entity.position.y)) {
                                Block targetBlock = floorBlocks.get(ThreadLocalRandom.current().nextInt(floorBlocks.size()));

                                entity.position.set(targetBlock.x + 0.5, targetBlock.y + 0.5);
                                entity.prevPosition.set(entity.position);
                                entity.velocity.set(0, 0);
                                entity.updateAABB();
                            }
                        }
                    }
                }

                boolean playerInExit = false;
                for (Block block : currentRoom.blocks) {
                    if (block.blockType == BlockType.Exit || block.blockType == BlockType.ClosedExit) {
                        AABB doorAABB = new AABB(block.x, block.y, block.x + 1, block.y + 1);
                        if (player.worldAABB.getPenetrationVector(doorAABB) != null) {
                            playerInExit = true;
                            break;
                        }
                    }
                }

                if (currentRoom.entities.stream().anyMatch(entity -> entity instanceof EnemyEntity && !entity.removed)) {
                    if (!currentRoom.isClosed() && !playerInExit)
                        currentRoom.closeRoom();
                } else {
                    if (currentRoom.isClosed())
                        currentRoom.openRoom();
                }
            }
        }
    }

    public void onPlayerDying(Player deadPlayer) {
        game.setScreen(new DeathScreen());
    }

    public void onPlayerDead(Player deadPlayer) {
        game.setWorld(null);
    }

    private void trySwapPlayer() {
        long now = System.currentTimeMillis();

        if (now - lastSwapTime < SWAP_COOLDOWN_MS)
            return;

        Player oldPlayer = this.player;

        this.player = (this.player == knight) ? mage : knight;

        this.player.position.set(oldPlayer.position);
        this.player.prevPosition.set(oldPlayer.prevPosition);
        this.player.velocity.set(oldPlayer.velocity);
        this.player.updateAABB();

        this.player.setInvincibilityTimer(0);

        if (game.getWorld() != null && game.getWorld().currentRoom != null) {
            Room room = game.getWorld().currentRoom;
            room.entities.remove(oldPlayer);
            room.entities.add(this.player);
        }

        this.lastSwapTime = now;
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