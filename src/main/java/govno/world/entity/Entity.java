package govno.world.entity;

import govno.utils.AABB;
import govno.GameInstance;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public abstract class Entity implements GameInstance {
    public final Vector2d position = new Vector2d();
    public final Vector2d prevPosition = new Vector2d();
    public final Vector2d velocity = new Vector2d();
    public double yaw = 0;
    public double prevYaw = 0;
    public final AABB modelAABB = new AABB(-0.5,  -0.5, 0.5, 0.5);
    public final AABB worldAABB = new AABB(this.modelAABB);

    public void tick() {
        this.prevPosition.set(this.position);
        this.prevYaw = this.yaw;
    }

    public void updateAABB() {
        this.worldAABB.set(this.modelAABB).move(this.position);
    }

    public abstract void onWorldCollision(Vector2dc out);

    public abstract void onEntityCollision(Entity other, Vector2dc out);
}
