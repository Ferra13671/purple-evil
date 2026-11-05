package stayablox.purpleevil.world;

import stayablox.purpleevil.GameInstance;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.block.Block;
import stayablox.purpleevil.world.block.BlockType;
import stayablox.purpleevil.world.entity.EnemyEntity;
import stayablox.purpleevil.world.entity.Entity;
import stayablox.purpleevil.world.entity.Player;
import stayablox.purpleevil.world.room.Room;
import stayablox.purpleevil.world.room.RoomType;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class World implements GameInstance {
    public static final double MAX_VELOCITY_STEP = 0.25;
    public static final double ACTIVE_RADIUS = 60.0;

    public final Camera camera = new Camera();
    public final List<Room> rooms;
    public Room currentRoom;

    public World(List<Room> rooms) {
        this.rooms = rooms;

        Room spawnRoom = this.rooms.stream()
                .filter(r -> r.roomType == RoomType.Spawn)
                .findFirst()
                .orElse(this.rooms.getFirst());

        this.currentRoom = spawnRoom;

        Player player = game.getGameSession().player;
        player.position.set(spawnRoom.centerX, spawnRoom.centerY);
        player.prevPosition.set(player.position);
        player.velocity.set(0, 0);
        player.updateAABB();

        this.currentRoom.entities.add(player);

        this.camera.position.set((float) player.position.x, (float) player.position.y);
        this.camera.prevPosition.set(this.camera.position);
    }

    public void removeEntity(Entity entity) {
        entity.removed = true;
        for (Room room : rooms)
            room.entities.remove(entity);
    }

    public void tick() {
        Player player = game.getGameSession().player;
        player.tick();

        AABB activeArea = new AABB(
                player.position.x - ACTIVE_RADIUS, player.position.y - ACTIVE_RADIUS,
                player.position.x + ACTIVE_RADIUS, player.position.y + ACTIVE_RADIUS
        );

        List<Room> activeRooms = new ArrayList<>();
        for (Room room : this.rooms) {
            if (room.intersects(activeArea)) {
                activeRooms.add(room);
            }
        }

        for (Room room : activeRooms) {
            room.tick();
        }

        tryChangeRoom(activeRooms, player);
        handleCombatRoomLogic(player);

        List<Entity> activeEntities = new ArrayList<>();
        List<Block> activeBlocks = new ArrayList<>();
        for (Room room : activeRooms) {
            activeEntities.addAll(room.entities);
            activeBlocks.addAll(room.blocks);
        }

        tickPhysics(activeEntities, activeBlocks);
        this.camera.tick();
    }

    private void tryChangeRoom(List<Room> activeRooms, Player player) {
        Room nextRoom = this.currentRoom;

        for (Room room : activeRooms) {
            if (room == this.currentRoom)
                continue;

            AABB roomBounds = room.getBounds();
            AABB pAABB = player.worldAABB;

            if (room.roomType.combat) {
                if (room.intersects(pAABB)) {
                    boolean overlappingDoor = false;
                    for (Block block : room.blocks) {
                        if (block.blockType == BlockType.Exit || block.blockType == BlockType.ClosedExit) {
                            if (pAABB.getMinX() < block.x + 1 && pAABB.getMaxX() > block.x &&
                                    pAABB.getMinY() < block.y + 1 && pAABB.getMaxY() > block.y) {
                                overlappingDoor = true;
                                break;
                            }
                        }
                    }

                    if (!overlappingDoor) {
                        int pX = (int) Math.floor(player.position.x);
                        int pY = (int) Math.floor(player.position.y);
                        boolean onFloor = false;

                        for (Block block : room.blocks) {
                            if (block.x == pX && block.y == pY) {
                                if (block.blockType == BlockType.Floor || block.blockType == BlockType.PortalFloor) {
                                    onFloor = true;
                                }
                                break;
                            }
                        }

                        if (onFloor) {
                            nextRoom = room;
                            break;
                        }
                    }
                }
            } else {
                if (roomBounds.contains(player.position.x, player.position.y)) {
                    if (nextRoom == this.currentRoom) {
                        nextRoom = room;
                    }
                }
            }
        }

        if (nextRoom != this.currentRoom) {
            this.currentRoom.entities.remove(player);
            this.currentRoom = nextRoom;
            this.currentRoom.entities.add(player);
        }
    }

    private void handleCombatRoomLogic(Player player) {
        Room currentRoom = this.currentRoom;

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
                    final double padding = 0.5;
                    AABB doorZone = new AABB(
                            block.x - padding, block.y - padding,
                            block.x + 1.0 + padding, block.y + 1.0 + padding
                    );
                    if (doorZone.contains(player.position.x, player.position.y)) {
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

    public void tickPhysics(List<Entity> entities, List<Block> blocks) {
        // ... (остается без изменений)
        if (entities.isEmpty()) return;

        double maxSpeed = 0;
        for (Entity e : entities)
            maxSpeed = Math.max(maxSpeed, e.velocity.length());

        int steps = Math.max((int) Math.ceil(maxSpeed / MAX_VELOCITY_STEP), 1);
        double stepDelta = 1.0 / steps;

        for (int step = 0; step < steps; step++) {
            for (Entity e : entities) {
                if (e.removed) continue;
                e.position.y += e.velocity.y * stepDelta;
                e.updateAABB();
            }
            for (Entity e : entities) {
                if (e.removed) continue;
                for (Block block : blocks) {
                    if (block.collisionBox == null) continue;
                    Vector2dc out = e.worldAABB.getPenetrationVector(block.collisionBox, false);
                    if (out != null) { e.onWorldCollision(out); e.updateAABB(); }
                }
            }

            for (Entity e : entities) {
                if (e.removed) continue;
                e.position.x += e.velocity.x * stepDelta;
                e.updateAABB();
            }
            for (Entity e : entities) {
                if (e.removed) continue;
                for (Block block : blocks) {
                    if (block.collisionBox == null) continue;
                    Vector2dc out = e.worldAABB.getPenetrationVector(block.collisionBox, true);
                    if (out != null) { e.onWorldCollision(out); e.updateAABB(); }
                }
            }

            for (int i = 0; i < entities.size(); i++) {
                Entity e1 = entities.get(i);
                if (e1.removed || e1.phasing) continue;

                for (int j = i + 1; j < entities.size(); j++) {
                    Entity e2 = entities.get(j);
                    if (e2.removed) continue;

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