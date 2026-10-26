package govno.screen;

import govno.GameInstance;
import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import org.joml.Vector4f;

public class Screen implements GameInstance {

    public void renderBackground() {
        renderBackground(1f);
    }

    public void renderBackground(float alphaMultiplier) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        boolean hasWorld = game.getWorld() != null;
        float a = Math.clamp(alphaMultiplier, 0f, 1f);

        Vector4f top;
        Vector4f bottom;
        if (hasWorld) {
            top = new Vector4f(0, 0, 0, 0.75f * a);
            bottom = new Vector4f(0, 0, 0, 0.75f * a);
        } else {
            top = new Vector4f(0f, 0f, 0f, a);
            bottom = new Vector4f(0.6823f, 0, 1f, a);
        }

        new ColoredRectBatch()
                .rectSized(0, 0, width, height, RectColors.verticalGradient(top, bottom))
                .build().tryDraw().close();
    }

    public void onOpen(int width, int height) {}

    public void onClose() {}

    public void resize(int width, int height) {}

    public void render(double mouseX, double mouseY) {}

    public void tick() {}

    public void onMousePress(double mouseX, double mouseY, int button) {}

    public void onMouseRelease(double mouseX, double mouseY, int button) {}

    public void onKeyPress(int key) {}

    public void onKeyRelease(int key) {}

    public void close() {
        game.setScreen(null);
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }
}