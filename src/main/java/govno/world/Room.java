package govno.world;

import govno.world.block.Block;
import govno.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Room {
    public final List<Block> blocks = new ArrayList<>();
    public final List<Entity> entities = new ArrayList<>();

    public Room(List<Block> blocks) {
        this.blocks.addAll(blocks);
    }

    public void tick() {
        for (Entity entity : this.entities) {
            entity.tick();
        }
    }
}
