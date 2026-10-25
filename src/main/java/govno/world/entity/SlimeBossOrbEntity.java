package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import org.joml.Vector2dc;

public class SlimeBossOrbEntity extends Entity {
    private final double damage;

    public SlimeBossOrbEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime-boss-orb")),
                new AABB(-0.5, -0.5, 0.5, 0.5)
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