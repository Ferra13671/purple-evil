package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.weapon.WeaponAttributes;
import org.joml.Vector2dc;

public abstract class OrbEntity extends Entity {
    protected final double baseDamage;
    protected final Attributes weaponAttrs;
    protected final Entity excludedEntity;
    protected final boolean hitPlayersOnly;
    private final AABB spawnRoomBounds;

    protected OrbEntity(String textureId, AABB modelAabb,
                        Vector2dc position, Vector2dc velocity,
                        double baseDamage, Attributes weaponAttrs,
                        Entity excludedEntity, boolean hitPlayersOnly) {
        super(new EntityModel(game.getEntityModelTemplates().templates.get(textureId)), modelAabb);
        this.position.set(position);
        this.prevPosition.set(position);
        this.velocity.set(velocity);
        this.baseDamage = baseDamage;
        this.weaponAttrs = weaponAttrs;
        this.excludedEntity = excludedEntity;
        this.hitPlayersOnly = hitPlayersOnly;
        this.spawnRoomBounds = (game.getWorld() != null && game.getWorld().currentRoom != null)
                ? game.getWorld().currentRoom.getBounds()
                : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (spawnRoomBounds != null && !spawnRoomBounds.contains(position.x, position.y)) {
            game.getWorld().removeEntity(this);
        }
    }

    @Override
    public void onWorldCollision(Vector2dc out) {
        game.getWorld().removeEntity(this);
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (!(other instanceof LivingEntity le)) return;
        if (le == this.excludedEntity) return;
        if (this.hitPlayersOnly && le != game.getGameSession().player) return;

        if (this.weaponAttrs != null) {
            le.damage(this.weaponAttrs.getCurrentValue(WeaponAttributes.DAMAGE));
            double poison = this.weaponAttrs.getCurrentValue(WeaponAttributes.POISONING);
            if (poison > 0) le.applyPoison(poison, 60);
        } else {
            le.damage(this.baseDamage);
        }

        game.getWorld().removeEntity(this);
    }
}