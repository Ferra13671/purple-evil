package govno.world.entity;

import govno.GameInstance;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;

public class PlayerInput implements GameInstance {
    public final Vector2d vector = new Vector2d();

    public void tick() {
        double xDelta = 0, yDelta = 0;

        xDelta += game.getWindow().isPressedKey(GLFW.GLFW_KEY_D) ? 1 : 0;
        xDelta -= game.getWindow().isPressedKey(GLFW.GLFW_KEY_A) ? 1 : 0;
        yDelta += game.getWindow().isPressedKey(GLFW.GLFW_KEY_W) ? 1 : 0;
        yDelta -= game.getWindow().isPressedKey(GLFW.GLFW_KEY_S) ? 1 : 0;

        this.vector.set(
                xDelta,
                yDelta
        );
    }
}
