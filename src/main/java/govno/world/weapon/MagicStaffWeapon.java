package govno.world.weapon;

import govno.world.attribute.Attributes;
import govno.world.entity.LivingEntity;
import govno.world.entity.MagicStaffChargeEntity;
import org.joml.Vector2d;

import java.util.HashMap;

public class MagicStaffWeapon extends Weapon {

    public MagicStaffWeapon() {
        super(
                "magic-staff",
                2f,
                new Attributes(new HashMap<>()),
                new HashMap<>()
        );
    }

    @Override
    public void startUse(LivingEntity entity) {
        game.getWorld().currentRoom.entities.add(new MagicStaffChargeEntity(
                entity,
                new Vector2d(entity.position),
                new Vector2d(
                        Math.cos(Math.toRadians(entity.yaw)),
                        Math.sin(Math.toRadians(entity.yaw))
                ).normalize()
        ));
    }
}
