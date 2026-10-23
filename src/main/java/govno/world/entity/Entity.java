package govno.world.entity;

import govno.AABB;
import govno.GameInstance;
import org.joml.Vector2d;

public abstract class Entity implements GameInstance {
    public final Vector2d position = new Vector2d();
    public final Vector2d prevPosition = new Vector2d();
    public final Vector2d velocity = new Vector2d();
    public double yaw = 0;
    public double prevYaw = 0;
    public final AABB modelAABB = new AABB(-0.5,  -0.5, 0.5, 0.5);
    public final AABB worldAABB = new AABB(this.modelAABB);

    public void tick() {
        startTick();
        endTick();
    }

    protected void startTick() {
        this.prevPosition.set(this.position);
        this.prevYaw = this.yaw;
    }

    protected void endTick() {
        this.worldAABB.set(this.modelAABB).move(this.position);
        this.position.add(this.velocity);
    }
}
