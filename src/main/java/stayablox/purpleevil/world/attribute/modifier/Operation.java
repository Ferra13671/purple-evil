package stayablox.purpleevil.world.attribute.modifier;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Operation {
    ADD("add"),
    MULTIPLY_BASE("multiply_base"),
    MULTIPLY_TOTAL("multiply_total");

    public final String id;

    public static Operation fromId(String id) {
        for (Operation operation : values())
            if (operation.id.equals(id))
                return operation;

        return null;
    }
}