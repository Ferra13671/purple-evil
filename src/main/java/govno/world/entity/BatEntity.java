package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.BatInput;
import govno.world.room.Room;
import org.joml.Vector2d;

public class BatEntity extends EnemyEntity {

    public BatEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("bat")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(game.getAttributesTemplates().templates.get("bat")),
                new Vector2d(0, 0),
                null
        );

        setInput(new BatInput(this, room));
    }
}