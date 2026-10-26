package govno.world.entity;

import govno.utils.AABB;
import org.joml.Vector2dc;

public class SlimeBossOrbEntity extends OrbEntity {
    public SlimeBossOrbEntity(Vector2dc position, Vector2dc velocity, double damage) {
        super("slime-boss-orb", new AABB(-0.5, -0.5, 0.5, 0.5),
                position, velocity, damage, null, null, true);
    }
}