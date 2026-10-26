package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.SlimeBossInput;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class SlimeBossEntity extends EnemyEntity {

    public SlimeBossEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime-boss")),
                new AABB(-2.0, -2.0, 2.0, 2.0),
                new Attributes(game.getAttributesTemplates().templates.get("slime-boss")),
                new Vector2d(0, 0),
                null
        );

        setInput(new SlimeBossInput(this, room));
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (this.isShocked() || this.dying) return;

        if (other == game.getGameSession().player && !this.phasing)
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.DAMAGE));
    }
}