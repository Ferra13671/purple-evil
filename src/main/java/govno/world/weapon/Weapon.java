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

    public Weapon(@NonNull String textureId, float size, @NonNull Attributes attributes, @NonNull Map<String, AttributeModifier> entityModifiers) {
        this.textureId = textureId;
        this.size = size;
        this.attributes = attributes;
        this.entityModifiers = entityModifiers;
    }

    public void tick(LivingEntity entity) {}

    public void startUse(LivingEntity entity) {}

    public void endUse(LivingEntity entity) {}

    public double getAttributeValue(String id) {
        return this.attributes.getCurrentValue(id);
    }
}
