package govno.world.entity.input;

import govno.Keybinds;

public class PlayerInput extends Input {

    public void tick() {
        double xDelta = 0, yDelta = 0;

        xDelta += Keybinds.RIGHT.isPressed() ? 1 : 0;
        xDelta -= Keybinds.LEFT.isPressed() ? 1 : 0;
        yDelta += Keybinds.FORWARD.isPressed() ? 1 : 0;
        yDelta -= Keybinds.BACK.isPressed() ? 1 : 0;

        this.vector.set(
                xDelta,
                yDelta
        );
    }
}
