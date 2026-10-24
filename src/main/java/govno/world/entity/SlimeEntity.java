package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.EnemyInput;
import govno.world.room.Room;
import org.joml.Vector2d;

import java.util.HashMap;

public class SlimeEntity extends LivingEntity {

    public SlimeEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime")),
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(new HashMap<>(){{
                    put(EntityAttributes.MAX_HEALTH, 60d);
                    put(EntityAttributes.REGENERATION, 0.2);
                    put(EntityAttributes.ACCELERATION, 1d);
                    put(EntityAttributes.SPEED, 0.05);
                    put(EnemyAttributes.DAMAGE, 25d);
                }}),
                60d,
                new Vector2d(0, 0),
                null
        );

        setInput(new EnemyInput(this, room));
    }
}
