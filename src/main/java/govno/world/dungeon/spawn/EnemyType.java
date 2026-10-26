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
    FlyingDemon(FlyingDemonEntity::new),
    CrossbowmanDemon(CrossbowmanDemonEntity::new),
    SlimeBoss(SlimeBossEntity::new),
    LichBoss(LichBossEntity::new),
    DemonBoos(DemonBossEntity::new);

    public final Function<Room, LivingEntity> enemyCreator;
}