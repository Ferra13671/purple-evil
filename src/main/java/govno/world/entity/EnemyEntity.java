package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.upgrades.Upgrade;
import govno.world.weapon.Weapon;
import lombok.NonNull;
import org.joml.Vector2d;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnemyEntity extends LivingEntity {
    private static final float UPGRADE_DROP_CHANCE = 0.17f;

    public EnemyEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, double health, Vector2d handPos, Weapon weapon) {
        super(model, aabb, attributes, health, handPos, weapon);
    }

    @Override
    public void onDying() {
        super.onDying();

        game.getGameSession().score += (int) getAttributeValue(EnemyAttributes.SCORE);

        if (ThreadLocalRandom.current().nextFloat() < UPGRADE_DROP_CHANCE) {
            Collection<Upgrade> allUpgrades = game.getUpgrades().upgrades.values();

            if (!allUpgrades.isEmpty()) {
                List<Upgrade> upgradeList = allUpgrades.stream().filter(Upgrade::spawning).toList();
                Upgrade randomUpgrade = upgradeList.get(ThreadLocalRandom.current().nextInt(upgradeList.size()));

                UpgradeEntity upgradeEntity = new UpgradeEntity(randomUpgrade);

                upgradeEntity.position.set(this.position);
                upgradeEntity.prevPosition.set(this.position);
                upgradeEntity.updateAABB();

                game.getWorld().currentRoom.entities.add(upgradeEntity);
            }
        }
    }
}
