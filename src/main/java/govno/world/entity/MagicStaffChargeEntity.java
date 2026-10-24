package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import org.joml.Vector2dc;

public class MagicStaffChargeEntity extends Entity {
    private final Entity shooter;

    public MagicStaffChargeEntity(Entity shooter, Vector2dc position, Vector2dc velocity) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("magic-staff-charge")),
                new AABB(-0.3, -0.3, 0.3, 0.3)
        );

        this.shooter = shooter;
        this.position.set(position);
        this.velocity.set(velocity);
    }

    @Override
    public void onWorldCollision(Vector2dc out) {
        game.getWorld().removeEntity(this);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other instanceof LivingEntity le && le != this.shooter) {
            le.damage(20);
            onWorldCollision(out);
        }
    }
}
