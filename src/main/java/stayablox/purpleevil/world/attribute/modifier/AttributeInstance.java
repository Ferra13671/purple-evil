package stayablox.purpleevil.world.attribute.modifier;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.modifiers.removeIf(m -> Objects.equals(m, modifier));
        this.modifiers.add(modifier);
        this.dirty = true;
    }

    public boolean removeModifier(AttributeModifier modifier) {
        boolean removed = this.modifiers.removeIf(m -> Objects.equals(m, modifier));
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
