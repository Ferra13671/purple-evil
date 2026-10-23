package govno.world;

import govno.world.block.Block;
import govno.world.entity.Entity;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public static final double MAX_VELOCITY_STEP = 0.25;

    public final List<Block> blocks = new ArrayList<>();
    public final List<Entity> entities = new ArrayList<>();

    public Room(List<Block> blocks) {
        this.blocks.addAll(blocks);
    }

    public void tick() {
        for (Entity entity : this.entities)
            entity.tick();

        tickPhysics();
    }

    private void tickPhysics() {
        double maxSpeed = 0;
        for (Entity entity : this.entities)
            maxSpeed = Math.max(maxSpeed, entity.velocity.length());

        int steps = Math.max((int) Math.ceil(maxSpeed / MAX_VELOCITY_STEP), 1);
        double stepDelta = 1.0 / steps;

        for (int step = 0; step < steps; step++) {

            for (Entity entity : this.entities) {
                entity.position.y += entity.velocity.y * stepDelta;
                entity.updateAABB();
            }

            for (Entity entity : this.entities) {
                for (Block block : this.blocks) {
                    if (block.collisionBox != null) {
                        Vector2dc out = entity.worldAABB.getPenetrationVector(block.collisionBox, false);
                        if (out != null) {
                            entity.onWorldCollision(out);
                            entity.updateAABB();
                        }
                    }
                }
            }



            for (Entity entity : this.entities) {
                entity.position.x += entity.velocity.x * stepDelta;
                entity.updateAABB();
            }

            for (Entity entity : this.entities) {
                for (Block block : this.blocks) {
                    if (block.collisionBox != null) {
                        Vector2dc out = entity.worldAABB.getPenetrationVector(block.collisionBox, true);
                        if (out != null) {
                            entity.onWorldCollision(out);
                            entity.updateAABB();
                        }
                    }
                }
            }



            for (int i = 0; i < this.entities.size(); i++) {
                for (int j = i + 1; j < this.entities.size(); j++) {
                    Entity e1 = this.entities.get(i);
                    Entity e2 = this.entities.get(j);

                    Vector2d outX = e1.worldAABB.getPenetrationVector(e2.worldAABB, true);
                    if (outX != null) {
                        e1.onEntityCollision(e2, outX);
                        e1.updateAABB();
                        e2.onEntityCollision(e1, outX.negate());
                        e2.updateAABB();
                    }

                    Vector2d outY = e1.worldAABB.getPenetrationVector(e2.worldAABB, false);
                    if (outY != null) {
                        e1.onEntityCollision(e2, outY);
                        e1.updateAABB();
                        e2.onEntityCollision(e1, outY.negate());
                        e2.updateAABB();
                    }
                }
            }
        }
    }
}
