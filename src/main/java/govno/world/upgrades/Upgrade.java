package govno.world.upgrades;

import govno.world.attribute.modifier.AttributeModifier;

import java.util.Map;

public record Upgrade(
        String name,
        int price,
        UpgradeType type,
        Map<String, AttributeModifier> modifiers
) {
}
