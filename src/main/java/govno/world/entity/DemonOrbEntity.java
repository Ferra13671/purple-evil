package govno.world.entity;

import govno.utils.AABB;
import org.joml.Vector2dc;

public class DemonOrbEntity extends OrbEntity {
    public DemonOrbEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super("demon-orb", new AABB(-0.4, -0.4, 0.4, 0.4),
                position, velocity, damage, null, null, true);
    }
}