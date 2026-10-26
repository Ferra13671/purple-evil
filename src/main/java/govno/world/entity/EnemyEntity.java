package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.upgrades.Upgrade;
import govno.world.weapon.Weapon;
import lombok.NonNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnemyEntity extends LivingEntity {
    private static final float UPGRADE_DROP_CHANCE = 0.17f;

    public boolean canDropUpgrades = true;

    public EnemyEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, Vector2d handPos, Weapon weapon) {
        super(model, aabb, attributes, handPos, weapon);
    }

    @Override
    public void onDying() {
        super.onDying();

        game.getGameSession().score += (int) getAttributeValue(EnemyAttributes.SCORE);

        // Проверяем флаг
        if (this.canDropUpgrades && ThreadLocalRandom.current().nextFloat() < UPGRADE_DROP_CHANCE) {
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

    @Override
    public void damage(double damage) {
        if (game.getWorld() == null || game.getWorld().currentRoom == null)
            return;
        if (!game.getWorld().currentRoom.entities.contains(this))
            return;

        super.damage(damage);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player) {
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.DAMAGE));
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.KNOCKBACK));

            if (this.model.hasAnimation("strike"))
                this.model.play("strike");
        }
    }
}