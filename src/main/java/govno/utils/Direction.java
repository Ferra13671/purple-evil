package govno.utils;

public enum Direction {
    UP, DOWN, LEFT, RIGHT;

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