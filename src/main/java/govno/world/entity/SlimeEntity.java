package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.SlimeInput;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

public class SlimeEntity extends EnemyEntity {

    public SlimeEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime")),
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(game.getAttributesTemplates().templates.get("slime")),
                60d,
                new Vector2d(0, 0),
                null
        );

        setInput(new SlimeInput(this, room));
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player)
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.DAMAGE));
    }
}
