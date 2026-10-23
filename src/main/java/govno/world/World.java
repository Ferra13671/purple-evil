package govno.world;

import govno.world.entity.Entity;
import govno.world.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class World {
    public final Player player = new Player();
    public final List<Entity> entities = new ArrayList<>(List.of(
            this.player
    ));

    public void tick() {
        for (Entity entity : this.entities) {
            entity.tick();
        }
    }
}
