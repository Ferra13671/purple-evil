package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.PlayerInput;
import govno.world.weapon.MagicStaffWeapon;
import org.joml.Vector2d;

import java.util.HashMap;

public class Player extends LivingEntity {

    public Player() {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("knight")),
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(new HashMap<>(){{
                    put(EntityAttributes.MAX_HEALTH, 100d);
                    put(EntityAttributes.REGENERATION, 0.4);
                    put(EntityAttributes.ACCELERATION, 0.6);
                    put(EntityAttributes.SPEED, 0.4);
                }}),
                100,
                new Vector2d(0.5, 1),
                new MagicStaffWeapon()
        );

        setInput(new PlayerInput());
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Получаем размеры окна
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        // 2. Центр экрана
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // 3. Позиция мыши
        double mouseX = game.getMouseListener().position.x;
        double mouseY = game.getMouseListener().position.y;

        // 4. Вектор от центра к мыши
        // Обрати внимание: centerY - mouseY, так как экранный Y идет вниз, а мировой — вверх
        double dx = mouseX - centerX;
        double dy = centerY - mouseY;

        // 5. Вычисляем угол (в радианах, от -PI до +PI)
        // 0 радиан = вправо, PI/2 = вверх, -PI/2 = вниз
        this.yaw = Math.toDegrees(Math.atan2(dy, dx));
    }
}
