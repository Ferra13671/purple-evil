package govno.utils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Direction {
    UP("up"),
    DOWN("down"),
    LEFT("left"),
    RIGHT("right");

    public final String id;

    public Direction opposite() {
        return switch (this) {
            case UP    -> DOWN;
            case DOWN  -> UP;
            case LEFT  -> RIGHT;
            case RIGHT -> LEFT;
        };
    }


    public int dx() { return this == RIGHT ? 1 : this == LEFT ? -1 : 0; }
    public int dy() { return this == UP    ? 1 : this == DOWN ? -1 : 0; }
}