package govno.world.room;

import java.util.List;

public record RoomTemplate(String name, List<BlockTemplate> blocks, int width, int height) {
}
