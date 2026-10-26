package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.DemonBossInput;
import govno.world.room.Room;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.List;

public class DemonBossEntity extends EnemyEntity {
    public final List<Entity> summonedMinions = new ArrayList<>();

    public DemonBossEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("demon-boss")),
                new AABB(-1.2, -1.7, 1.2, 1.7),
                new Attributes(game.getAttributesTemplates().templates.get("demon-boss")),
                new Vector2d(0, 0),
                null
        );
        setInput(new DemonBossInput(this, room));
    }

    @Override
    public void damage(double damage) {
        summonedMinions.removeIf(e -> e.removed);

        if (hasAliveMinions())
            return;
        super.damage(damage);
    }

    @Override
    public boolean isInvincible() {
        return !hasAliveMinions() && super.isInvincible();
    }

    private boolean hasAliveMinions() {
        for (Entity e : summonedMinions) {
            if (e.removed) continue;
            if (e instanceof LivingEntity le && le.isDying()) continue;
            return true;
        }
        return false;
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (this.dying) return;

        if (other == game.getGameSession().player) {
            game.getGameSession().player.damage(getAttributeValue(EnemyAttributes.DAMAGE));
            game.getGameSession().player.applyKnockback(
                    this.position, getAttributeValue(EnemyAttributes.KNOCKBACK));
        }
    }
}