package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.SlimeInput;
import govno.world.room.Room;
import org.joml.Vector2d;

public class SlimeEntity extends EnemyEntity {

    public SlimeEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime")),
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(game.getAttributesTemplates().templates.get("slime")),
                new Vector2d(0, 0),
                null
        );

        setInput(new SlimeInput(this, room));
    }
}
