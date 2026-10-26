package govno.world.entity.input;

import govno.Settings;

public class PlayerInput extends Input {

    public void tick() {
        double xDelta = 0, yDelta = 0;

        xDelta += Settings.RIGHT.isPressed() ? 1 : 0;
        xDelta -= Settings.LEFT.isPressed() ? 1 : 0;
        yDelta += Settings.FORWARD.isPressed() ? 1 : 0;
        yDelta -= Settings.BACK.isPressed() ? 1 : 0;

        this.vector.set(
                xDelta,
                yDelta
        );
    }
}
