package stayablox.purpleevil.world.upgrades;

import stayablox.purpleevil.world.attribute.modifier.AttributeModifier;

import java.util.Map;

public record Upgrade(
        String name,
        int price,
        UpgradeType type,
        boolean spawning,
        Map<String, AttributeModifier> modifiers
) {
}
