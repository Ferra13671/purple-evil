package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.texture.GLTex;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import stayablox.purpleevil.GameSession;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;

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
        float spacing = 15;

        float totalHeight = buttonHeight * 3 + spacing * 2;
        float startY = halfHeight - totalHeight / 2;

        this.buttons.add(new Button(
                "Play", halfWidth - (buttonWidth / 2), startY, buttonWidth, buttonHeight, () -> {
            game.setGameSession(new GameSession());
            game.getGameSession().startGame();
        }
        ));
        this.buttons.add(new Button(
                "Settings", halfWidth - (buttonWidth / 2), startY + buttonHeight + spacing,
                buttonWidth, buttonHeight,
                () -> game.setScreen(new SettingsScreen(this))
        ));
        this.buttons.add(new Button(
                "Quit", halfWidth - (buttonWidth / 2), startY + (buttonHeight + spacing) * 2,
                buttonWidth, buttonHeight, CometGLFW::close
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        renderBackground();
        renderTitle();

        for (Button button : buttons)
            button.render(mouseX, mouseY);
    }

    private void renderTitle() {
        GLTex titleTex = game.getTextures().titleTexture;
        if (titleTex == null) return;

        int screenWidth = game.getWindow().getFramebufferWidth();
        int screenHeight = game.getWindow().getFramebufferHeight();

        float texW = titleTex.getWidth();
        float texH = titleTex.getHeight();
        float texAspect = texW / texH;

        float drawW = screenWidth * 0.6f;
        float drawH = drawW / texAspect;

        float maxH = screenHeight * 0.30f;
        if (drawH > maxH) {
            drawH = maxH;
            drawW = drawH * texAspect;
        }

        float drawX = (screenWidth - drawW) / 2f;
        float drawY = screenHeight * 0.08f;

        new BasicTextureBatch()
                .texture(titleTex)
                .rectSized(drawX, drawY, drawW, drawH, TextureBorder.DEFAULT)
                .build()
                .tryDraw()
                .close();
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons)
            b.onMouseRelease(mouseX, mouseY, button);
    }
}