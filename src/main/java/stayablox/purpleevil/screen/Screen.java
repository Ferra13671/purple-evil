package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.texture.GLTexture;
import stayablox.purpleevil.GameInstance;
import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import org.joml.Vector4f;

public class Screen implements GameInstance {

    /** Maximum parallax shift as a fraction of the screen dimension. */
    private static final float PARALLAX_MAX_OFFSET = 0.06f;

    public void renderBackground() {
        renderBackground(1f, 0, 0);
    }

    public void renderBackground(double mouseX, double mouseY) {
        renderBackground(1f, mouseX, mouseY);
    }

    public void renderBackground(float alphaMultiplier) {
        renderBackground(alphaMultiplier, 0, 0);
    }

    public void renderBackground(float alphaMultiplier, double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        if (game.getWorld() == null)  {
            double nx = Math.clamp((mouseX / width) * 2 - 1, -1.0, 1.0);
            double ny = Math.clamp((mouseY / height) * 2 - 1, -1.0, 1.0);

            BasicTextureBatch batch = new BasicTextureBatch()
                    .texture(game.getTextures().menuAtlas);

            renderMenuLayer(nx, ny, 0.25f, width, height, game.getTextures().menuFarTexture, batch);
            renderMenuLayer(nx, ny, 0.55f, width, height, game.getTextures().menuMiddleTexture, batch);
            renderMenuLayer(nx, ny, 0.90f, width, height, game.getTextures().menuNear2Texture, batch);
            renderMenuLayer(nx, ny, 1.00f, width, height, game.getTextures().menuNear1Texture, batch);

            batch.build().tryDraw().close();
        }

        if (showDarkBackground()) {
            float a = Math.clamp(alphaMultiplier, 0f, 1f);
            new ColoredRectBatch()
                    .rectSized(0, 0, width, height, RectColors.oneColor(new Vector4f(0, 0, 0, 0.6f * a)))
                    .build()
                    .tryDraw()
                    .close();
        }
    }

    private void renderMenuLayer(double nx, double ny, float factor, int width, int height, GLTexture texture, BasicTextureBatch batch) {
        if (texture == null)
            return;

        float tWidth = texture.getWidth();
        float tHeight = texture.getHeight();
        float texAspect = tWidth / tHeight;
        float screenAspect = (float) width / height;

        float dWidth, dHeight;
        if (screenAspect > texAspect) {
            dWidth = width;
            dHeight = width / texAspect;
        } else {
            dHeight = height;
            dWidth = height * texAspect;
        }

        float scaleFactor = 1f + 2f * PARALLAX_MAX_OFFSET * factor;
        dWidth *= scaleFactor;
        dHeight *= scaleFactor;

        float maxOffsetX = width * PARALLAX_MAX_OFFSET * factor;
        float maxOffsetY = height * PARALLAX_MAX_OFFSET * factor;

        float offsetX = (float) (-nx * maxOffsetX);
        float offsetY = (float) (-ny * maxOffsetY);

        float drawX = (width - dWidth) / 2f + offsetX;
        float drawY = (height - dHeight) / 2f + offsetY;

        batch.rectSized(drawX, drawY, dWidth, dHeight, game.getTextures().menuAtlas.getBorder(texture));
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

    public boolean showDarkBackground() {
        return true;
    }
}