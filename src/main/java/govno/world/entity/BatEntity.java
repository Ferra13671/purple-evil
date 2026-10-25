package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.BatInput;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.HashMap;

public class BatEntity extends EnemyEntity {

    public BatEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("bat")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(new HashMap<>(){{
                    put(EntityAttributes.MAX_HEALTH, 30d);
                    put(EntityAttributes.REGENERATION, 0.01);
                    put(EntityAttributes.ACCELERATION, 2d);
                    put(EntityAttributes.SPEED, 0.6);
                    put(EntityAttributes.INVINCIBILITY_TIME, 0d);
                    put(EnemyAttributes.DAMAGE, 15d);
                    put(EnemyAttributes.SCORE, 15d);
                }}),
                30d,
                new Vector2d(0, 0),
                null
        );

        setInput(new BatInput(this, room));
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player) {
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.DAMAGE));
            this.model.play("strike");
        }
    }
}