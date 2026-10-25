package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.KnightSkeletonInput;
import govno.world.room.Room;
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