package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.KnightSkeletonInput;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

public class KnightSkeletonEntity extends EnemyEntity {

    public KnightSkeletonEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("knight-skeleton")),
                new AABB(-0.5, -0.5, 0.5, 0.5),
                new Attributes(game.getAttributesTemplates().templates.get("knight-skeleton")),
                new Vector2d(0, 0),
                null
        );

        setInput(new KnightSkeletonInput(this, room));
    }
}