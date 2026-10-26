package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.BatInput;
import stayablox.purpleevil.world.room.Room;
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