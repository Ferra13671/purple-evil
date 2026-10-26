package govno.screen;

import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ConfirmScreen extends Screen {
    private final Screen previousScreen;
    private final String title;
    private final String message;
    private final Runnable onConfirm;

    private final List<Button> buttons = new ArrayList<>();

    public ConfirmScreen(Screen previousScreen, String title, String message, Runnable onConfirm) {
        this.previousScreen = previousScreen;
        this.title = title;
        this.message = message;
        this.onConfirm = onConfirm;
    }

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float btnW = 200f;
        float btnH = 50f;
        float spacing = 20f;

        buttons.add(new Button(
                "Yes",
                halfWidth - btnW - spacing / 2f, halfHeight + 20f, btnW, btnH,
                onConfirm
        ));
        buttons.add(new Button(
                "No",
                halfWidth + spacing / 2f, halfHeight + 20f, btnW, btnH,
                () -> game.setScreen(this.previousScreen)
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        new ColoredRectBatch()
                .rectSized(0, 0, width, height,
                        RectColors.verticalGradient(
                                new Vector4f(0, 0, 0, 0.75f),
                                new Vector4f(0, 0, 0, 0.75f)))
                .build().tryDraw().close();

        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        float titleH = game.getRenderer().bigFont.getTextHeight();
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, height / 4f - titleH - 40f).withShadow(true))
                .build().tryDraw().close();

        float msgW = game.getRenderer().smallFont.getTextWidth(message);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(message, (width - msgW) / 2f, height / 3f).withShadow(true))
                .build().tryDraw().close();

        for (Button b : buttons) b.render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons) b.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        game.setScreen(this.previousScreen);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}