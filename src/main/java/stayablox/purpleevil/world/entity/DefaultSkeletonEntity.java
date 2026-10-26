package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.DefaultSkeletonInput;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

public class DefaultSkeletonEntity extends EnemyEntity {
    private final DefaultSkeletonInput skeletonInput;

    public DefaultSkeletonEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("default-skeleton")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(game.getAttributesTemplates().templates.get("default-skeleton")),
                new Vector2d(0, 0),
                null
        );

        this.skeletonInput = new DefaultSkeletonInput(this, room);
        setInput(this.skeletonInput);
    }

    @Override
    public void damage(double damage) {
        boolean wasDamaged = this.health > 0 && !isInvincible() && !dying;
        super.damage(damage);

        if (wasDamaged && this.health > 0)
            this.skeletonInput.onHit();
    }
}