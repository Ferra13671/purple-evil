package govno.world.entity;

import govno.model.EntityModel;
import govno.world.attribute.Attributes;
import govno.world.attribute.EntityAttributes;
import govno.world.entity.input.PlayerInput;

import java.util.HashMap;

public class Player extends LivingEntity {

    public Player() {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("knight")),
                new PlayerInput(),
                new Attributes(new HashMap<>(){{
                    put(EntityAttributes.MAX_HEALTH, 100d);
                    put(EntityAttributes.REGENERATION, 0.4);
                    put(EntityAttributes.ACCELERATION, 0.6);
                    put(EntityAttributes.SPEED, 0.4);
                }}),
                100
        );
    }

}
