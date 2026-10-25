package govno.world.weapon;

import govno.world.attribute.Attributes;
import govno.world.entity.LivingEntity;
import govno.world.entity.MagicStaffChargeEntity;
import org.joml.Vector2d;

import java.util.HashMap;

public class MagicStaffWeapon extends Weapon {

    public MagicStaffWeapon() {
        super("magic-staff", 2f, 800, new Attributes(new HashMap<>()), new HashMap<>());
    }

    @Override
    protected void attack(LivingEntity entity) {
        super.attack(entity);

        float angleRad = (float) Math.toRadians(entity.yaw - 45);
        float halfSize = getSize() / 2.0f;

        float localTipX = halfSize + 0.5f;
        float localTipY = halfSize + 0.5f;

        float tipOffsetX = localTipX * (float) Math.cos(angleRad) - localTipY * (float) Math.sin(angleRad);
        float tipOffsetY = localTipX * (float) Math.sin(angleRad) + localTipY * (float) Math.cos(angleRad);

        float handWorldX = (float) (entity.position.x - 0.5 + entity.getHandPos().x);
        float handWorldY = (float) (entity.position.y - 0.5 + entity.getHandPos().y);

        Vector2d spawnPos = new Vector2d(
                handWorldX + tipOffsetX,
                handWorldY + tipOffsetY
        );

        game.getWorld().currentRoom.entities.add(new MagicStaffChargeEntity(
                entity,
                spawnPos,
                new Vector2d(
                        Math.cos(Math.toRadians(entity.yaw)),
                        Math.sin(Math.toRadians(entity.yaw))
                ).normalize()
        ));
    }
}