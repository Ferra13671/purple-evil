package govno;

import com.ferra13671.cometrenderer.plugins.glfw.callback.KeyCallback;
import com.ferra13671.cometrenderer.plugins.glfw.callback.MousePosCallback;
import org.joml.Vector2d;

public class MouseListener implements MousePosCallback, KeyCallback, GameInstance {
    public final Vector2d position = new Vector2d();

    @Override
    public void onPress(int key) {
        if (game.getScreen() != null)
            game.getScreen().onMousePress(position.x, position.y, key);
    }

    @Override
    public void onRelease(int key) {
        if (game.getScreen() != null)
            game.getScreen().onMouseRelease(position.x, position.y, key);
    }

    @Override
    public void onPos(double mouseX, double mouseY) {
        this.position.set(mouseX, mouseY);
    }
}
