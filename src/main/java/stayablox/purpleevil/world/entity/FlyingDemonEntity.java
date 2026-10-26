package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.FlyingDemonInput;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class FlyingDemonEntity extends EnemyEntity {
    private final FlyingDemonInput demonInput;

    public FlyingDemonEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("flying-demon")),
                new AABB(-0.4, -0.4, 0.4, 0.4),
                new Attributes(game.getAttributesTemplates().templates.get("flying-demon")),
                new Vector2d(0, 0),
                null
        );

        this.demonInput = new FlyingDemonInput(this, room);
        setInput(this.demonInput);
    }

    @Override
    public void onWorldCollision(Vector2dc out) {
        super.onWorldCollision(out);
        this.demonInput.onWallHit(out);
    }
}