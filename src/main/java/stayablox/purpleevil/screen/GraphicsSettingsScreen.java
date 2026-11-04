package stayablox.purpleevil.screen;

import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;

import java.util.ArrayList;
import java.util.List;

public class GraphicsSettingsScreen extends Screen {
    private final Screen previousScreen;
    private final List<Button> buttons = new ArrayList<>();

    public GraphicsSettingsScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();
        float btnW = 200f;
        float btnH = 50f;
        buttons.add(new Button("Back", (width - btnW) / 2f, height - btnH - 30f,
                btnW, btnH, () -> game.setScreen(previousScreen)));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        renderBackground(mouseX, mouseY);

        String title = "GRAPHICS SETTINGS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        String emptyText = "No graphics settings available yet.";
        float textW = game.getRenderer().smallFont.getTextWidth(emptyText);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(emptyText, (width - textW) / 2f, height / 2f).withShadow(true))
                .build().tryDraw().close();

        for (Button b : buttons) b.render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons) b.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        game.setScreen(previousScreen);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}