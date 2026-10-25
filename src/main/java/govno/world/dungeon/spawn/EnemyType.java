package govno.world.dungeon.spawn;

import govno.world.entity.*;
import govno.world.room.Room;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public enum EnemyType {
    Slime(SlimeEntity::new),
    Bat(BatEntity::new),
    DefaultSkeleton(DefaultSkeletonEntity::new),
    KillerSkeleton(KillerSkeletonEntity::new),
    KnightSkeleton(KnightSkeletonEntity::new),
    SlimeBoss(SlimeBossEntity::new);

    public final Function<Room, LivingEntity> enemyCreator;
}