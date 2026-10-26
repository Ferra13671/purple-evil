package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import org.joml.Vector2dc;

public class DemonArrowEntity extends Entity {
    private final double damage;

    public DemonArrowEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("demon-arrow")),
                new AABB(-0.1, -0.1, 0.1, 0.1)
        );
        this.position.set(position);
        this.prevPosition.set(position);
        this.velocity.set(velocity);
        this.damage = damage;

        this.yaw = Math.toDegrees(Math.atan2(velocity.y(), velocity.x()));
        this.prevYaw = this.yaw;
    }

    @Override
    public void onWorldCollision(Vector2dc out) {
        game.getWorld().removeEntity(this);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player) {
            game.getGameSession().player.damage(damage);
            game.getWorld().removeEntity(this);
        }
    }
}