package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import org.joml.Vector2dc;

public class DemonOrbEntity extends Entity {
    private final double damage;

    public DemonOrbEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("demon-orb")),
                new AABB(-0.4, -0.4, 0.4, 0.4)
        );
        this.position.set(position);
        this.prevPosition.set(position);
        this.velocity.set(velocity);
        this.damage = damage;
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