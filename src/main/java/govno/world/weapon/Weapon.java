package govno.world.weapon;

import govno.GameInstance;
import govno.world.attribute.Attributes;
import govno.world.attribute.modifier.AttributeModifier;
import govno.world.entity.LivingEntity;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

public class Weapon implements GameInstance {
    @Getter
    protected final String textureId;
    @Getter
    protected final float size;
    @Getter
    protected final Attributes attributes;
    @Getter
    protected final Map<String, AttributeModifier> entityModifiers;

    @Getter
    protected boolean usingWeapon = false;
    protected long lastUseTime = 0;

    public Weapon(@NonNull String textureId, float size, @NonNull Attributes attributes, @NonNull Map<String, AttributeModifier> entityModifiers) {
        this.textureId = textureId;
        this.size = size;
        this.attributes = attributes;
        this.entityModifiers = entityModifiers;
    }

    public int getCooldownMS() {
        return (int) (getAttributeValue(WeaponAttributes.COOLDOWN_MS) / getAttributeValue(WeaponAttributes.ATTACK_SPEED));
    }

    public boolean canUse() {
        return System.currentTimeMillis() - this.lastUseTime >= getCooldownMS();
    }

    public float getCooldownProgress() {
        if (getCooldownMS() <= 0)
            return 1f;

        long elapsed = System.currentTimeMillis() - this.lastUseTime;
        return Math.min(1f, elapsed / (float) getCooldownMS());
    }

    public void tick(LivingEntity entity) {
        if (this.usingWeapon && canUse())
            attack(entity);
    }

    public void startUse(LivingEntity entity) {
        this.usingWeapon = true;

        if (canUse())
            attack(entity);
    }

    public void endUse(LivingEntity entity) {
        this.usingWeapon = false;
    }

    protected void attack(LivingEntity entity) {
        this.lastUseTime = System.currentTimeMillis();
    }

    public double getAttributeValue(String id) {
        return this.attributes.getCurrentValue(id);
    }
}