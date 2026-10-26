package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.GameInstance;
import lombok.NonNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public abstract class Entity implements GameInstance {
    public final Vector2d position = new Vector2d();
    public final Vector2d prevPosition = new Vector2d();
    public final Vector2d velocity = new Vector2d();
    public double yaw = 0;
    public double prevYaw = 0;
    public final EntityModel model;
    public final AABB modelAABB;
    public final AABB worldAABB;

    public boolean phasing = false;
    public boolean removed = false;

    public Entity(EntityModel model, @NonNull AABB modelAABB) {
        this.model = model;
        this.modelAABB = modelAABB;
        this.worldAABB = new AABB(modelAABB);
    }

    public void tick() {
        this.prevPosition.set(this.position);
        this.prevYaw = this.yaw;

        if (this.model != null)
            this.model.tick(game.getTicker().getTickDelay() / 1000f);
    }

    public void updateAABB() {
        this.worldAABB.set(this.modelAABB).move(this.position);
    }

    public abstract void onWorldCollision(Vector2dc out);

    public abstract void onEntityCollision(Entity other, Vector2dc out);
}
