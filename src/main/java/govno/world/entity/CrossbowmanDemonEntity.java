package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.CrossbowmanDemonInput;
import govno.world.room.Room;
import org.joml.Vector2d;

public class CrossbowmanDemonEntity extends EnemyEntity {

    public CrossbowmanDemonEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("crossbowman-demon")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(game.getAttributesTemplates().templates.get("crossbowman-demon")),
                new Vector2d(0, 0),
                null
        );

        setInput(new CrossbowmanDemonInput(this, room));
    }
}