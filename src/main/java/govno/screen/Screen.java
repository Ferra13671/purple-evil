package govno.screen;

import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import govno.GameInstance;
import govno.renderer.RectColors;
import govno.renderer.batch.impl.BasicTextureBatch;
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

        if (hasWorld) {
            float a = Math.clamp(alphaMultiplier, 0f, 1f);

            new ColoredRectBatch()
                    .rectSized(0, 0, width, height, RectColors.verticalGradient(
                            new Vector4f(0, 0, 0, 0.75f * a),
                            new Vector4f(0, 0, 0, 0.75f * a)
                    ))
                    .build()
                    .tryDraw()
                    .close();
        } else {
            var menuTex = game.getTextures().menuTexture;

            float tWidth = menuTex.getWidth();
            float tHeight = menuTex.getHeight();
            float texAspect    = tWidth / tHeight;
            float screenAspect = (float) width / height;

            float dWidth, dHeight;
            if (screenAspect > texAspect) {
                dWidth = width;
                dHeight = width / texAspect;
            } else {
                dHeight = height;
                dWidth = height * texAspect;
            }

            float drawX = (width - dWidth) / 2f;
            float drawY = (height - dHeight) / 2f;

            new BasicTextureBatch()
                    .texture(menuTex)
                    .rectSized(drawX, drawY, dWidth, dHeight, TextureBorder.DEFAULT)
                    .build()
                    .tryDraw()
                    .close();
        }
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