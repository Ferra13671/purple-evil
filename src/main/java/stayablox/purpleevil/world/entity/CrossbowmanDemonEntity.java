package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.CrossbowmanDemonInput;
import stayablox.purpleevil.world.room.Room;
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