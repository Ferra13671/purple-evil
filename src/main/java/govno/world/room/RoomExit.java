package govno.world.room;

import govno.utils.Direction;

public record RoomExit(int x, int y, int width, int height, Direction side) {
}
