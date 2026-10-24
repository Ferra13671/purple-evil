package govno.world.weapon;

import govno.world.attribute.Attributes;

import java.util.HashMap;

public class MagicStaffWeapon extends Weapon {

    public MagicStaffWeapon() {
        super(
                "magic-staff",
                2f,
                new Attributes(new HashMap<>()),
                new HashMap<>()
        );
    }
}
