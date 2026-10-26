package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.SlimeInput;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector4f;

import java.util.concurrent.ThreadLocalRandom;

public class SlimeEntity extends EnemyEntity {
    public final Vector4f color = new Vector4f();

    public SlimeEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("slime")),
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(game.getAttributesTemplates().templates.get("slime")),
                new Vector2d(0, 0),
                null
        );

        setInput(new SlimeInput(this, room));

        float roll = ThreadLocalRandom.current().nextFloat();
        if (roll < 0.60f) {
            this.color.set(0.5f, 1f, 0.5f, 1f);
        } else if (roll < 0.80f) {
            this.color.set(0.6f, 0.9f, 1f, 1f);   // голубой
        } else if (roll < 0.95f) {
            this.color.set(1f, 0.5f, 0.5f, 1f);   // красный
        } else {
            this.color.set(0.8f, 0.6f, 1f, 1f);   // фиолетовый
        }
    }
}
