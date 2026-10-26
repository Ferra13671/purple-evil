package govno.screen;

import govno.GameInstance;

public class Screen implements GameInstance {

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
