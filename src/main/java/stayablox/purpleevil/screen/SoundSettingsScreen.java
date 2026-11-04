package stayablox.purpleevil.screen;

import stayablox.purpleevil.Settings;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;

import java.util.ArrayList;
import java.util.List;

public class SoundSettingsScreen extends Screen {
    public static final float SLIDER_WIDTH = 500f;
    public static final float SLIDER_HEIGHT = 44f;
    private static final float SLIDER_GAP = 15f;

    private final Screen previousScreen;
    private final List<Slider> sliders = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();

    public SoundSettingsScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();
        sliders.clear();

        float startX = (width - SLIDER_WIDTH) / 2f;

        int sliderCount = 3;
        float totalSlidersHeight = (sliderCount * SLIDER_HEIGHT) + ((sliderCount - 1) * SLIDER_GAP);
        float startY = (height - totalSlidersHeight) / 2f - 20f;

        sliders.add(new Slider(
                "Music Volume", startX, startY, SLIDER_WIDTH, SLIDER_HEIGHT,
                () -> Settings.MUSIC_VOLUME,
                v -> Settings.MUSIC_VOLUME = (float) v
        ));

        sliders.add(new Slider(
                "World Sounds", startX, startY + SLIDER_HEIGHT + SLIDER_GAP, SLIDER_WIDTH, SLIDER_HEIGHT,
                () -> Settings.WORLD_SOUNDS_VOLUME,
                v -> Settings.WORLD_SOUNDS_VOLUME = (float) v
        ));

        sliders.add(new Slider(
                "UI Sounds", startX, startY + 2 * (SLIDER_HEIGHT + SLIDER_GAP), SLIDER_WIDTH, SLIDER_HEIGHT,
                () -> Settings.UI_SOUNDS_VOLUME,
                v -> Settings.UI_SOUNDS_VOLUME = (float) v
        ));

        float btnW = 200f;
        float btnH = 50f;
        buttons.add(new Button("Back", (width - btnW) / 2f, height - btnH - 30f,
                btnW, btnH, () -> game.setScreen(previousScreen)));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        for (Slider s : sliders) s.updateIfDragging(mouseX);

        int width = game.getWindow().getFramebufferWidth();

        renderBackground(mouseX, mouseY);

        String title = "SOUND SETTINGS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        for (Slider s : sliders) s.render(mouseX, mouseY);
        for (Button b : buttons) b.render(mouseX, mouseY);
    }

    @Override
    public void onMousePress(double mouseX, double mouseY, int button) {
        for (Slider s : sliders) s.onMousePress(mouseX, mouseY, button);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        boolean anySliderHit = sliders.stream().anyMatch(s -> s.contains(mouseX, mouseY));
        boolean wasDragging = sliders.stream().anyMatch(Slider::isDragging);

        for (Slider s : sliders) s.onMouseRelease(mouseX, mouseY, button);

        if (anySliderHit || wasDragging) return;

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