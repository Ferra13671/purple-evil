package govno.world;

import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.entity.Player;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class World {
    public final Camera camera = new Camera();
    public final Player player = new Player();
    public Room currentRoom = createRoomTest(30, 30);

    public World() {
        this.currentRoom.entities.add(this.player);
    }

    public void tick() {
        if (this.currentRoom != null)
            this.currentRoom.tick();

        this.camera.tick();
    }

    //TODO убрать
    private static Room createRoomTest(int xSize, int ySize) {
        int halfXSize = xSize / 2;
        int halfYSize = ySize / 2;

        return new Room(
                IntStream.range(-halfXSize, halfXSize).boxed().flatMap(x ->
                        IntStream.range(-halfYSize, halfYSize).mapToObj(y -> {
                            boolean isWall = (x == -halfXSize || x == halfXSize - 1 || y == -halfYSize || y == halfYSize - 1);
                            return isWall
                                    ? new Block(BlockType.Wall, x, y, new String[]{"wall"})
                                    : new Block(BlockType.Floor, x, y, new String[]{"floor"});
                        })
                ).collect(Collectors.toList())
        );
    }
}
