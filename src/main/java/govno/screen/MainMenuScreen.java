package govno.screen;

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

        this.buttons.add(new Button(
                "Play", (width / 2f) - 100, (height / 2f) - 25, 200, 50, () -> {
                    game.setWorld(new World());
                    game.setScreen(null);
                }
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
