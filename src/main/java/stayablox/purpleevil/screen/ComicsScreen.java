package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.texture.GLTexture;
import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class ComicsScreen extends Screen {
    private enum State {
        FADE_IN,
        SHOWING,
        FADE_OUT
    }

    private final Screen previousScreen;
    private final Runnable onComplete;
    private final List<String> pages;
    private int currentPage = 0;

    private State state = State.FADE_IN;
    private long stateTimer = 0;

    private static final long FADE_DURATION_MS = 1500;

    public ComicsScreen(Screen previousScreen, String... pages) {
        this(previousScreen, null, Arrays.asList(pages));
    }

    public ComicsScreen(Screen previousScreen, List<String> pages) {
        this(previousScreen, null, pages);
    }

    public ComicsScreen(Screen previousScreen, Runnable onComplete, String... pages) {
        this(previousScreen, onComplete, Arrays.asList(pages));
    }

    public ComicsScreen(Screen previousScreen, Runnable onComplete, List<String> pages) {
        this.previousScreen = previousScreen;
        this.onComplete = onComplete;
        this.pages = pages;
    }

    @Override
    public void onOpen(int width, int height) {
        this.state = State.FADE_IN;
        this.stateTimer = System.currentTimeMillis();
        this.currentPage = 0;
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        if (!pages.isEmpty() && currentPage < pages.size()) {
            String pageId = pages.get(currentPage);
            GLTexture tex = game.getTextures().getComicsTexture(pageId);

            if (tex != null) {
                float texW = tex.getWidth();
                float texH = tex.getHeight();
                float texAspect = texW / texH;
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
                        .texture(game.getTextures().comicsAtlas)
                        .rectSized(drawX, drawY, dWidth, dHeight,
                                game.getTextures().comicsAtlas.getBorder(tex))
                        .build().tryDraw().close();
            }
        }

        long elapsed = System.currentTimeMillis() - stateTimer;
        float alpha = 0f;

        if (state == State.FADE_IN) {
            if (elapsed >= FADE_DURATION_MS) {
                state = State.SHOWING;
                alpha = 0f;
            } else {
                alpha = 1f - (float) elapsed / FADE_DURATION_MS;
            }
        } else if (state == State.FADE_OUT) {
            if (elapsed >= FADE_DURATION_MS) {
                close();
                return;
            } else {
                alpha = (float) elapsed / FADE_DURATION_MS;
            }
        }

        if (alpha > 0.001f) {
            new ColoredRectBatch()
                    .rectSized(0, 0, width, height, RectColors.verticalGradient(
                            new Vector4f(0, 0, 0, alpha),
                            new Vector4f(0, 0, 0, alpha)
                    ))
                    .build().tryDraw().close();
        }

        if (state == State.SHOWING) {
            String hint = (currentPage == pages.size() - 1) ? "Click to finish" : "Click to continue";
            float textW = game.getRenderer().smallFont.getTextWidth(hint);
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(hint, (width - textW) / 2f, height - 40f)
                            .withShadow(true))
                    .build().tryDraw().close();
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && state == State.SHOWING) {
            if (currentPage < pages.size() - 1) {
                currentPage++;
                state = State.FADE_IN;
                stateTimer = System.currentTimeMillis();
            } else {
                state = State.FADE_OUT;
                stateTimer = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void close() {
        if (onComplete != null) {
            onComplete.run();
        } else {
            game.setScreen(previousScreen);
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}