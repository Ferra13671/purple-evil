package govno;

import com.ferra13671.cometrenderer.plugins.glfw.callback.KeyCallback;
import govno.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class KeyboardListener implements KeyCallback, GameInstance {
    
    @Override
    public void onPress(int key) {
        Screen screen = game.getScreen();
        if (screen != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                screen.close();
                return;
            }

            game.getScreen().onKeyPress(key);
        }
    }

    @Override
    public void onRelease(int key) {
        if (game.getScreen() != null)
            game.getScreen().onKeyRelease(key);
    }
}
