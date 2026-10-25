package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.KillerSkeletonInput;
import govno.world.room.Room;
import org.joml.Vector2d;

public class KillerSkeletonEntity extends EnemyEntity {

    public KillerSkeletonEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("killer-skeleton")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(game.getAttributesTemplates().templates.get("killer-skeleton")),
                new Vector2d(0, 0),
                null
        );

        setInput(new KillerSkeletonInput(this, room));
    }
}