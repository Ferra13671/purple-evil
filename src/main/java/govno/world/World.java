package govno.world;

import govno.GameInstance;
import govno.world.block.Block;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.List;

public class World implements GameInstance {
    public static final double MAX_VELOCITY_STEP = 0.25;

    public final Camera camera = new Camera();
    public final Player player = new Player();
    public List<Room> rooms = new DungeonGenerator(
            game.getRoomTemplates(),
            System.nanoTime()
    ).generate();
    public Room currentRoom = this.rooms.getFirst();

    public World() {
        this.currentRoom.entities.add(this.player);
    }

    public void tick() {
        this.player.tick();

        if (this.currentRoom != null) {
            this.currentRoom.tick();
            tickPhysics(this.currentRoom);
        }

        for (Room room : this.rooms) {
            if (room.intersects(this.player.worldAABB) && room != this.currentRoom) {
                this.currentRoom.entities.remove(this.player);
                this.currentRoom = room;
                this.currentRoom.entities.add(this.player);
            }
        }

        this.camera.tick();
    }

    public void tickPhysics(Room room) {
        List<Entity> entities = room.entities;
        List<Block> blocks = room.blocks;

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

                    Vector2d outX = e1.worldAABB.getPenetrationVector(e2.worldAABB, true);
                    if (outX != null) {
                        e1.onEntityCollision(e2, outX); e1.updateAABB();
                        e2.onEntityCollision(e1, outX.negate()); e2.updateAABB();
                    }

                    Vector2d outY = e1.worldAABB.getPenetrationVector(e2.worldAABB, false);
                    if (outY != null) {
                        e1.onEntityCollision(e2, outY); e1.updateAABB();
                        e2.onEntityCollision(e1, outY.negate()); e2.updateAABB();
                    }
                }
            }
        }
    }
}
