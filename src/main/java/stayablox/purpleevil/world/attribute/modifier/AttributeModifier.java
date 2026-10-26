package stayablox.purpleevil.world.attribute.modifier;

public record AttributeModifier(String id, double amount, Operation operation) {

    public AttributeModifier(AttributeModifier modifier) {
        this(modifier.id, modifier.amount, modifier.operation);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}