package stayablox.purpleevil.world.weapon;

import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.LivingEntity;
import stayablox.purpleevil.world.entity.MagicStaffChargeEntity;
import org.joml.Vector2d;

import java.util.HashMap;

public class MagicStaffWeapon extends Weapon {

    public MagicStaffWeapon() {
        super("magic-staff", 1.5f, new Attributes(new HashMap<>(){{
            put(WeaponAttributes.DAMAGE, 20d);
            put(WeaponAttributes.ATTACK_SPEED, 1d);
            put(WeaponAttributes.COOLDOWN_MS, 800d);
            put(WeaponAttributes.POISONING, 0d);
        }}), new HashMap<>());
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
                ).normalize(),
                this.attributes
        ));
    }
}