package stayablox.purpleevil.screen;

import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenuScreen extends Screen {
    public static final float BUTTON_WIDTH = 400f;
    public static final float BUTTON_HEIGHT = 50f;
    public static final float BUTTON_GAP = 15f;

    private final Screen previousScreen;
    private final List<Button> buttons = new ArrayList<>();

    public SettingsMenuScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();

        float startX = (width - BUTTON_WIDTH) / 2f;
        float totalH = (3 * BUTTON_HEIGHT) + (2 * BUTTON_GAP) + 40f;
        float startY = (height - totalH) / 2f;

        buttons.add(new Button("Sound Settings", startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT,
                () -> game.setScreen(new SoundSettingsScreen(this))));
        
        buttons.add(new Button("Graphics Settings", startX, startY + BUTTON_HEIGHT + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT,
                () -> game.setScreen(new GraphicsSettingsScreen(this))));
        
        buttons.add(new Button("Keybinds", startX, startY + (BUTTON_HEIGHT + BUTTON_GAP) * 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                () -> game.setScreen(new KeybindSettingsScreen(this))));

        float backY = startY + (BUTTON_HEIGHT + BUTTON_GAP) * 3 + 20f;
        buttons.add(new Button("Back", startX, backY, BUTTON_WIDTH, BUTTON_HEIGHT,
                () -> game.setScreen(previousScreen)));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        
        renderBackground(mouseX, mouseY);

        String title = "SETTINGS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        for (Button b : buttons)
            b.render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons)
            b.onMouseRelease(mouseX, mouseY, button);
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