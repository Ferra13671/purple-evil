package govno;

import com.ferra13671.cometrenderer.plugins.glfw.callback.KeyCallback;
import com.ferra13671.cometrenderer.plugins.glfw.callback.MousePosCallback;
import govno.utils.KeyBind;
import govno.utils.KeyType;
import org.joml.Vector2d;

public class MouseListener implements MousePosCallback, KeyCallback, GameInstance {
    public final Vector2d position = new Vector2d();

    @Override
    public void onPress(int key) {
        if (game.getScreen() != null)
            game.getScreen().onMousePress(position.x, position.y, key);
        else
            KeyBind.keybinds.get(KeyType.Mouse).forEach(k -> {
                if (k.getKey() == key)
                    k.setPressed(true);
            });
    }

    @Override
    public void onRelease(int key) {
        if (game.getScreen() != null)
            game.getScreen().onMouseRelease(position.x, position.y, key);
        else
            KeyBind.keybinds.get(KeyType.Mouse).forEach(k -> {
                if (k.getKey() == key)
                    k.setPressed(false);
            });
    }

    @Override
    public void onPos(double mouseX, double mouseY) {
        this.position.set(mouseX, mouseY);
    }
}
