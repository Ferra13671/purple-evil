package govno.world.attribute.modifier;

public record AttributeModifier(double amount, Operation operation) {

    public AttributeModifier(AttributeModifier modifier) {
        this(modifier.amount, modifier.operation);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}