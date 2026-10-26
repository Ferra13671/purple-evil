package govno.world.entity;

import govno.utils.AABB;
import govno.world.attribute.Attributes;
import org.joml.Vector2dc;

public class MagicStaffChargeEntity extends OrbEntity {
    public MagicStaffChargeEntity(Entity shooter, Vector2dc position, Vector2dc velocity,
                                  Attributes weaponAttributes) {
        super("magic-staff-charge", new AABB(-0.3, -0.3, 0.3, 0.3),
                position, velocity, 0, weaponAttributes, shooter, false);
    }
}