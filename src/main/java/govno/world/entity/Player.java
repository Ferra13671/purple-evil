package govno.world.entity;

import govno.model.EntityModel;
import govno.world.entity.input.PlayerInput;

public class Player extends LivingEntity {

    public Player() {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("knight")),
                new PlayerInput()
        );
    }

}
