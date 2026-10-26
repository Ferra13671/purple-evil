package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.entity.input.LichInput;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class LichBossEntity extends EnemyEntity {
    public final List<Entity> summonedMinions = new ArrayList<>();

    public LichBossEntity(Room room) {
        super(
                new EntityModel(game.getEntityModelTemplates().templates.get("lich-boss")),
                new AABB(-1, -2, 1, 2),
                new Attributes(game.getAttributesTemplates().templates.get("lich-boss")),
                new Vector2d(0, 0),
                null
        );

        setInput(new LichInput(this, room));
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
}