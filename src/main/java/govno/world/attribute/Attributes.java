package govno.world.attribute;


import govno.world.attribute.modifier.AttributeInstance;
import govno.world.attribute.modifier.AttributeModifier;

import java.util.HashMap;
import java.util.Map;

public class Attributes {
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

    public void removeModifier(String attribute, String id) {
        get(attribute).removeModifier(id);
    }

    public void clearAllModifiers() {
        this.attributes.values().forEach(AttributeInstance::clearModifiers);
    }
}
