package govno.world;

import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.entity.SlimeEntity;
import govno.world.room.Room;
import govno.world.room.RoomType;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonWorld extends World {

    public DungeonWorld() {
        super(new DungeonGenerator(game.getRoomTemplates(), System.nanoTime()).generate());

        spawnEnemies();
    }

    private void spawnEnemies() {
        Random random = new Random();

        for (Room room : this.rooms) {
            if (room.roomType != RoomType.Combat) continue;

            List<Block> floorBlocks = new ArrayList<>();
            for (Block b : room.blocks) {
                if (b.blockType == BlockType.Floor) {
                    floorBlocks.add(b);
                }
            }

            if (floorBlocks.isEmpty()) continue;

            int slimeCount = 1 + random.nextInt(3);
            slimeCount = Math.min(slimeCount, floorBlocks.size());

            for (int j = 0; j < slimeCount; j++) {
                Block spawnBlock = floorBlocks.remove(random.nextInt(floorBlocks.size()));

                SlimeEntity slime = new SlimeEntity(
                        new Vector2d(0, 0),
                        null,
                        room
                );

                slime.position.set(spawnBlock.x + 0.5, spawnBlock.y + 0.5);
                slime.prevPosition.set(slime.position);
                slime.updateAABB();

                room.entities.add(slime);
            }
        }
    }
}