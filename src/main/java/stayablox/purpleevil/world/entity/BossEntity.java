package stayablox.purpleevil.world.entity;

import lombok.NonNull;
import org.joml.Vector2d;
import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.attribute.Attributes;
import stayablox.purpleevil.world.weapon.Weapon;

public class BossEntity extends EnemyEntity {

    public BossEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, Vector2d handPos, Weapon weapon) {
        super(model, aabb, attributes, handPos, weapon);
    }
}
