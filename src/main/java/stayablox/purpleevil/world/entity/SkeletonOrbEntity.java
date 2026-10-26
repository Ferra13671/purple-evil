package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.utils.AABB;
import org.joml.Vector2dc;

public class SkeletonOrbEntity extends OrbEntity {
    public SkeletonOrbEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super("skeleton-orb", new AABB(-0.3, -0.3, 0.3, 0.3),
                position, velocity, damage, null, null, true);
    }
}