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
        // 1. Угол, под которым повернут посох (как в WorldRenderer)
        float angleRad = (float) Math.toRadians(entity.yaw - 45);
        float halfSize = getSize() / 2.0f;

        // 2. Локальные координаты кончика посоха
        // В рендере посох рисуется от -halfSize до halfSize и сдвигается на (0.5, 0.5)
        float localTipX = halfSize + 0.5f;
        float localTipY = halfSize + 0.5f;

        // 3. Поворачиваем локальный кончик на угол посоха
        float tipOffsetX = localTipX * (float) Math.cos(angleRad) - localTipY * (float) Math.sin(angleRad);
        float tipOffsetY = localTipX * (float) Math.sin(angleRad) + localTipY * (float) Math.cos(angleRad);

        // 4. Вычисляем мировые координаты руки (как в WorldRenderer)
        float handWorldX = (float) (entity.position.x - 0.5 + entity.getHandPos().x);
        float handWorldY = (float) (entity.position.y - 0.5 + entity.getHandPos().y);

        // 5. Итоговая позиция спавна снаряда
        Vector2d spawnPos = new Vector2d(
                handWorldX + tipOffsetX,
                handWorldY + tipOffsetY
        );

        // Спавним снаряд с правильной позицией и скоростью
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