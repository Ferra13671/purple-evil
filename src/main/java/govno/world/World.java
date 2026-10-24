package govno.world;

import govno.GameInstance;
import govno.utils.AABB;
import govno.world.block.Block;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import govno.world.room.RoomType;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;

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

        for (Room room : activeRooms) {
            if (room.intersects(player.worldAABB) && room != this.currentRoom) {
                this.currentRoom.entities.remove(player);
                this.currentRoom = room;
                this.currentRoom.entities.add(player);
                break;
            }
        }

        List<Entity> activeEntities = new ArrayList<>();
        List<Block> activeBlocks = new ArrayList<>();
        for (Room room : activeRooms) {
            activeEntities.addAll(room.entities);
            activeBlocks.addAll(room.blocks);
        }

        tickPhysics(activeEntities, activeBlocks);
        this.camera.tick();
    }

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