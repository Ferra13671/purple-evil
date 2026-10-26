package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class ShopEntity extends Entity {

    public ShopEntity(Vector2dc position) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("shop")),
                new AABB(-1.5, -1, 1.5, 1)
        );
        this.position.set(position);
        this.prevPosition.set(position);
        this.updateAABB();
    }

    @Override
    public void onWorldCollision(Vector2dc out) {}

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (!(other instanceof LivingEntity le)) return;

        Vector2d push = new Vector2d(out).negate();
        le.position.add(push);
        le.updateAABB();

        double len = push.length();
        if (len < 1e-12) return;

        double nx = push.x / len;
        double ny = push.y / len;
        double vn = le.velocity.x * nx + le.velocity.y * ny;
        if (vn < 0) {
            le.velocity.add(-nx * vn, -ny * vn);
        }
    }
}