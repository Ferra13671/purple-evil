package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.weapon.WeaponAttributes;
import org.joml.Vector2dc;

public class MagicStaffChargeEntity extends Entity {
    private final Entity shooter;
    private final Attributes weaponAttributes;

    public MagicStaffChargeEntity(Entity shooter, Vector2dc position, Vector2dc velocity, Attributes weaponAttributes) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("magic-staff-charge")),
                new AABB(-0.3, -0.3, 0.3, 0.3)
        );

        this.shooter = shooter;
        this.position.set(position);
        this.velocity.set(velocity);
        this.weaponAttributes = weaponAttributes;
    }

    @Override
    public void onWorldCollision(Vector2dc out) {
        game.getWorld().removeEntity(this);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other instanceof LivingEntity le && le != this.shooter) {
            le.damage(this.weaponAttributes.getCurrentValue(WeaponAttributes.DAMAGE));

            double poison = this.weaponAttributes.getCurrentValue(WeaponAttributes.POISONING);
            if (poison > 0)
                le.applyPoison(poison, 60);

            onWorldCollision(out);
        }
    }
}
