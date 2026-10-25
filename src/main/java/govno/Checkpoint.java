package govno;

import govno.world.attribute.modifier.AttributeModifier;

import java.util.List;
import java.util.Map;

public record Checkpoint(
        double knightHealth,
        double mageHealth,
        Map<String, List<AttributeModifier>> knightMods, Map<String, List<AttributeModifier>> mageMods,
        int score, int level
) {
}
