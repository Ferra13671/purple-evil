package stayablox.purpleevil.world.attribute;


import stayablox.purpleevil.world.attribute.modifier.AttributeInstance;
import stayablox.purpleevil.world.attribute.modifier.AttributeModifier;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Attributes {
    @Getter
    private final Map<String, AttributeInstance> attributes;

    public Attributes(Map<String, Double> baseValues) {
        this.attributes = new HashMap<>();
        baseValues.forEach((t, d) -> this.attributes.put(t, new AttributeInstance(d)));
    }

    public double getCurrentValue(String attribute) {
        return get(attribute).getCurrentValue();
    }

    public AttributeInstance get(String attribute) {
        return this.attributes.get(attribute);
    }

    public void addModifier(String attribute, AttributeModifier modifier) {
        get(attribute).addModifier(modifier);
    }

    public void removeModifier(String attribute, AttributeModifier modifier) {
        get(attribute).removeModifier(modifier);
    }

    public void clearAllModifiers() {
        this.attributes.values().forEach(AttributeInstance::clearModifiers);
    }

    public Map<String, List<AttributeModifier>> saveModifiers() {
        Map<String, List<AttributeModifier>> result = new HashMap<>();
        this.attributes.forEach((key, instance) -> {
            result.put(key, new ArrayList<>(instance.getModifiers()));
        });
        return result;
    }

    public void loadModifiers(Map<String, List<AttributeModifier>> snapshot) {
        this.attributes.forEach((key, instance) -> {
            instance.clearModifiers();
            if (snapshot.containsKey(key)) {
                snapshot.get(key).forEach(instance::addModifier);
            }
        });
    }
}
