package govno;

import govno.model.EntityModel;
import govno.screen.DeathScreen;
import govno.screen.ShopScreen;
import govno.utils.AABB;
import govno.world.LobbyWorld;
import govno.world.attribute.Attributes;
import govno.world.attribute.modifier.AttributeInstance;
import govno.world.attribute.modifier.AttributeModifier;
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
import govno.world.upgrades.Upgrade;
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

    private boolean worldClearPending = false;

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

    public float getSwapCooldownProgress() {
        long elapsed = System.currentTimeMillis() - lastSwapTime;
        if (elapsed >= SWAP_COOLDOWN_MS) return 0f;
        return 1f - (float) elapsed / SWAP_COOLDOWN_MS;
    }

    public void applyUpgrade(Upgrade upgrade) {
        switch (upgrade.type()) {
            case Player -> {
                applyUpgrade(upgrade, this.knight.getAttributes());
                applyUpgrade(upgrade, this.mage.getAttributes());
            }
            case Weapon -> {
                if (this.knight.getWeapon() != null)
                    applyUpgrade(upgrade, this.knight.getWeapon().getAttributes());
                if (this.mage.getWeapon() != null)
                    applyUpgrade(upgrade, this.mage.getWeapon().getAttributes());
            }
        }
    }

    private void applyUpgrade(Upgrade upgrade, Attributes attributes) {
        if (attributes == null) return;

        upgrade.modifiers().forEach((attributeName, modifier) -> {
            if (attributes.get(attributeName) != null)
                attributes.addModifier(attributeName, new AttributeModifier(modifier));
            else
                System.err.printf("Attribute '%s' not found!%n", attributeName);
        });
    }

    public int getUpgradePurchaseCount(String upgradeId) {
        Upgrade upgrade = game.getUpgrades().upgrades.get(upgradeId);
        if (upgrade == null || upgrade.modifiers().isEmpty()) return 0;

        Attributes attrs = switch (upgrade.type()) {
            case Player -> knight.getAttributes();
            case Weapon -> {
                if (knight.getWeapon() != null) yield knight.getWeapon().getAttributes();
                yield mage.getWeapon() != null ? mage.getWeapon().getAttributes() : null;
            }
        };
        if (attrs == null) return 0;

        int raw = 0;
        for (AttributeInstance inst : attrs.getAttributes().values()) {
            for (AttributeModifier mod : inst.getModifiers()) {
                if (mod.id().equals(upgradeId)) raw++;
            }
        }
        return raw / upgrade.modifiers().size();
    }

    public int getCurrentPrice(Upgrade upgrade) {
        int purchased = getUpgradePurchaseCount(upgrade.name());
        return (int) Math.round(upgrade.price() * Math.pow(2, purchased));
    }

    public boolean purchaseUpgrade(Upgrade upgrade) {
        int price = getCurrentPrice(upgrade);
        if (score < price) return false;
        score -= price;
        applyUpgrade(upgrade);
        return true;
    }

    private void saveCheckpoint() {
        this.checkpoint = new Checkpoint(
                this.knight.getHealth(), this.mage.getHealth(),
                this.knight.getAttributes().saveModifiers(), this.mage.getAttributes().saveModifiers(),
                this.score, this.currentLevel
        );
    }

    public void tick() {
        if (this.worldClearPending) {
            game.setWorld(null);
            this.worldClearPending = false;
        }

        if (game.getWorld() != null) {
            if (Settings.PLAYER_SWAP.isPressed()) {
                trySwapPlayer();
                Settings.PLAYER_SWAP.setPressed(false);
            }

            if (game.getWorld() instanceof LobbyWorld && game.getScreen() == null && Settings.SHOP.isPressed()) {
                game.setScreen(new ShopScreen());
                Settings.SHOP.setPressed(false);
            }

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
        this.worldClearPending = true;
    }

    private void trySwapPlayer() {
        long now = System.currentTimeMillis();

        if (now - lastSwapTime < SWAP_COOLDOWN_MS)
            return;

        Player oldPlayer = this.player;

        this.player = (this.player == knight) ? this.mage : this.knight;

        this.player.setHealth(oldPlayer.getHealth());
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
        int rooms = 1;
        int tunnelLength = 10;
        int minMobs = 1;
        int maxMobs = 1;

        EnemyType bossType = null;
        Map<EnemyType, SpawnProperty> spawns = new HashMap<>();

        switch (level) {
            case 1, 2 -> {
                rooms = 3 + level;
                minMobs = 4 + (level / 2);
                maxMobs = minMobs + 2 + level;

                spawns.put(EnemyType.Slime, new SpawnProperty(3 + level, 1.0));
                spawns.put(EnemyType.Bat, new SpawnProperty(3 + level, 1.0));
            }
            case 3 -> bossType = EnemyType.SlimeBoss;
            case 4, 5 -> {
                rooms = 4 + level / 4;
                minMobs = 4 + (level / 3);
                maxMobs = minMobs + 2 + (int) (level / 1.5);

                spawns.put(EnemyType.DefaultSkeleton, new SpawnProperty(2 + (level / 2), 1.5));
                spawns.put(EnemyType.KillerSkeleton, new SpawnProperty(1 + (level / 2), 1.0));
                spawns.put(EnemyType.KnightSkeleton, new SpawnProperty(1, 0.4));

                spawns.put(EnemyType.Slime, new SpawnProperty(1, 0.1));
                spawns.put(EnemyType.Bat, new SpawnProperty(1, 0.1));
            }
            case 6 -> bossType = EnemyType.LichBoss;
            case 7, 8 -> {
                rooms = 5 + level / 3;
                minMobs = 4 + (level / 4);
                maxMobs = minMobs + 2 + (level / 8);

                spawns.put(EnemyType.FlyingDemon, new SpawnProperty(2 + (level / 2), 2));
                spawns.put(EnemyType.CrossbowmanDemon, new SpawnProperty(2 + (level / 2), 1));

                spawns.put(EnemyType.DefaultSkeleton, new SpawnProperty(1, 0.15));
                spawns.put(EnemyType.KillerSkeleton, new SpawnProperty(1, 0.10));
                spawns.put(EnemyType.KnightSkeleton, new SpawnProperty(1, 0.05));

                spawns.put(EnemyType.Bat, new SpawnProperty(1, 0.05));
            }
            case 9 -> bossType = EnemyType.DemonBoos;
        }

        return new DungeonProperties(tunnelLength, rooms, 0.05, spawns, minMobs, maxMobs, bossType);
    }
}