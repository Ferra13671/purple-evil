package govno.world.dungeon.spawn;

import govno.world.entity.LivingEntity;
import govno.world.entity.SlimeEntity;
import govno.world.room.Room;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public enum EnemyType {
    Slime(SlimeEntity::new);

    public Function<Room, LivingEntity> enemyCreator;
}
