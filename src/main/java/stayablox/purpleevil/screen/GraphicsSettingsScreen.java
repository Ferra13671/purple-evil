package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.VsyncMode;
import stayablox.purpleevil.Settings;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GraphicsSettingsScreen extends Screen {
    public static final float ROW_WIDTH = 500f;
    public static final float ROW_HEIGHT = 44f;
    private static final float ROW_GAP = 10f; // Чуть уменьшили отступ, чтобы всё влезло

    private final Screen previousScreen;
    private final List<Button> buttons = new CopyOnWriteArrayList<>();
    private final List<Slider> sliders = new CopyOnWriteArrayList<>();

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
        sliders.clear();

        float startX = (width - ROW_WIDTH) / 2f;


        int elementsCount = 8;
        float totalHeight = elementsCount * ROW_HEIGHT + (elementsCount - 1) * ROW_GAP;
        float startY = (height - totalHeight) / 2f - 20f;

        float currentY = startY;

        sliders.add(new Slider(
                "Camera Zoom", startX, currentY, ROW_WIDTH, ROW_HEIGHT,
                () -> (Settings.CAMERA_ZOOM - 15.0) / (60.0 - 15.0),
                v -> Settings.CAMERA_ZOOM = (int) (15 + v * (60 - 15))
        ));
        currentY += ROW_HEIGHT + ROW_GAP;

        sliders.add(new Slider(
                "Minimap Size", startX, currentY, ROW_WIDTH, ROW_HEIGHT,
                () -> (Settings.MINIMAP_SIZE - 100.0) / (300.0 - 100.0),
                v -> Settings.MINIMAP_SIZE = (float) (100 + v * (300 - 100))
        ));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("Action Buttons: " + (Settings.SHOW_ACTION_BUTTONS ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.SHOW_ACTION_BUTTONS = !Settings.SHOW_ACTION_BUTTONS;
            resize(width, height);
        }));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("Vignette: " + (Settings.SHOW_VIGNETTE ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.SHOW_VIGNETTE = !Settings.SHOW_VIGNETTE;
            resize(width, height);
        }));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("Minimap: " + (Settings.SHOW_MINIMAP ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.SHOW_MINIMAP = !Settings.SHOW_MINIMAP;
            resize(width, height);
        }));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("Shadows: " + (Settings.SHOW_SHADOWS ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.SHOW_SHADOWS = !Settings.SHOW_SHADOWS;
            resize(width, height);
        }));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("Fog: " + (Settings.SHOW_FOG ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.SHOW_FOG = !Settings.SHOW_FOG;
            resize(width, height);
        }));
        currentY += ROW_HEIGHT + ROW_GAP;

        buttons.add(new Button("VSync: " + (Settings.VSYNC ? "ON" : "OFF"),
                startX, currentY, ROW_WIDTH, ROW_HEIGHT, () -> {
            Settings.VSYNC = !Settings.VSYNC;
            CometGLFW.setVSync(Settings.VSYNC ? VsyncMode.Enabled : VsyncMode.Disabled);
            resize(width, height);
        }));

        float btnW = 200f;
        float btnH = 50f;
        buttons.add(new Button("Back", (width - btnW) / 2f, height - btnH - 20f, // Чуть приподняли кнопку Назад
                btnW, btnH, () -> game.setScreen(previousScreen)));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        for (Slider s : sliders) s.updateIfDragging(mouseX);

        int width = game.getWindow().getFramebufferWidth();

        renderBackground(mouseX, mouseY);

        String title = "GRAPHICS SETTINGS";
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