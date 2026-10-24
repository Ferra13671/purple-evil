package govno.world.attribute.modifier;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class AttributeInstance {
    @Getter
    private final List<AttributeModifier> modifiers = new ArrayList<>();

    @Getter
    private final double baseValue;

    private double currentValue;
    private boolean dirty = true;

    public AttributeInstance(double baseValue) {
        this.baseValue = baseValue;
    }

    public double getCurrentValue() {
        if (this.dirty)
            this.currentValue = updateValue();

        return this.currentValue;
    }

    private double updateValue() {
        double value = this.baseValue;
        for (AttributeModifier m : this.modifiers) {
            if (m.operation() == Operation.ADD) {
                value += m.amount();
            }
        }

        double multiBaseSum = 0;
        for (AttributeModifier m : this.modifiers) {
            if (m.operation() == Operation.MULTIPLY_BASE) {
                multiBaseSum += m.amount();
            }
        }
        value *= (1.0 + multiBaseSum);

        for (AttributeModifier m : this.modifiers) {
            if (m.operation() == Operation.MULTIPLY_TOTAL) {
                value *= (1.0 + m.amount());
            }
        }

        this.dirty = false;
        return value;
    }

    public void addModifier(AttributeModifier modifier) {
        this.modifiers.removeIf(m -> m.id().equals(modifier.id()));
        this.modifiers.add(modifier);
        this.dirty = true;
    }

    public boolean removeModifier(String id) {
        boolean removed = this.modifiers.removeIf(m -> m.id().equals(id));
        if (removed)
            this.dirty = true;

        return removed;
    }

    public void clearModifiers() {
        if (!modifiers.isEmpty()) {
            modifiers.clear();
            dirty = true;
        }
    }
}
