package govno.world.entity;

import govno.model.Animation;
import govno.model.EntityModel;
import govno.model.EntityModelTemplate;
import govno.model.Sprite;
import govno.utils.AABB;
import govno.utils.Direction;
import govno.world.upgrades.Upgrade;
import org.joml.Vector2dc;

import java.util.List;
import java.util.Map;

public class UpgradeEntity extends Entity {
    private final Upgrade upgrade;

    public UpgradeEntity(Upgrade upgrade) {
        super(
                createUpgradeEntityModel(upgrade),
                new AABB(-0.5, -0.5, 0.5, 0.5)
        );

        this.upgrade = upgrade;
    }

    @Override
    public void onWorldCollision(Vector2dc out) {

    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player) {
            game.getGameSession().applyUpgrade(this.upgrade);
            game.getWorld().removeEntity(this);
        }
    }

    private static EntityModel createUpgradeEntityModel(Upgrade upgrade) {
        Map<String, Animation> map = Map.of(
                "stay", new Animation("stay", true, List.of(new Animation.Frame(new Sprite("upgrade-" + upgrade.name()), 1)))
        );

        return new EntityModel(new EntityModelTemplate(
                "upgrade-entity",
                Map.of(
                        Direction.UP, map,
                        Direction.DOWN, map,
                        Direction.LEFT, map,
                        Direction.RIGHT, map
                )
        ));
    }
}
