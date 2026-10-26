package stayablox.purpleevil.world.upgrades;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UpgradeType {
    Player("player"),
    Weapon("weapon");

    public final String id;

    public static UpgradeType fromId(String id) {
        for (UpgradeType upgradeType : values())
            if (upgradeType.id.equals(id))
                return upgradeType;

        return null;
    }
}
