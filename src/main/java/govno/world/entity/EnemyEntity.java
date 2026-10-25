package govno.world.entity;

import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.weapon.Weapon;
import lombok.NonNull;
import org.joml.Vector2d;

public class EnemyEntity extends LivingEntity {
    public EnemyEntity(@NonNull EntityModel model, @NonNull AABB aabb, @NonNull Attributes attributes, double health, Vector2d handPos, Weapon weapon) {
        super(model, aabb, attributes, health, handPos, weapon);
    }
}
