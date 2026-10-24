package govno.screen;

import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.world.World;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen extends Screen {
    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        this.buttons.clear();

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        float buttonWidth = 200;
        float buttonHeight = 50;

        this.buttons.add(new Button(
                "Play", halfWidth - (buttonWidth / 2), halfHeight - (buttonHeight / 2), buttonWidth, buttonHeight, () -> {
                    game.setWorld(new World());
                    game.setScreen(null);
                }
        ));
        this.buttons.add(new Button(
                "Settings", halfWidth - (buttonWidth / 2), halfHeight + (buttonHeight / 2) + 10, buttonWidth, buttonHeight, () -> {}
        ));
        this.buttons.add(new Button(
                "Quit", halfWidth - (buttonWidth / 2), halfHeight + (buttonHeight * 1.5f) + 20, buttonWidth, buttonHeight, CometGLFW::close
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        new ColoredRectBatch()
                .rectSized(0, 0, game.getWindow().getFramebufferWidth(), game.getWindow().getFramebufferHeight(), RectColors.verticalGradient(new Vector4f(0f, 0f, 0f, 1f), new Vector4f(0.6823f, 0, 1f, 1f)))
                .build()
                .tryDraw()
                .close();

        for (Button button : buttons)
            button.render(mouseX, mouseY);
    }

    @Override
    public void onMousePress(double mouseX, double mouseY, int button) {
        for (Button b : buttons)
            b.onMousePress(mouseX, mouseY, button);
    }
}
