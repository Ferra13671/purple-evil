package govno.screen;

import com.ferra13671.cometrenderer.CometRenderer;
import govno.GameInstance;
import govno.renderer.batch.impl.BasicRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class Button implements GameInstance {
    private final String text;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final Runnable pressRunnable;

    private final AABB aabb;

    public Button(String text, float x, float y, float width, float height, Runnable pressRunnable) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pressRunnable = pressRunnable;

        this.aabb = new AABB(x, y, x + width, y + height);
    }

    public void render(double mouseX, double mouseY) {
        Vector4f backGroundColor = contains(mouseX, mouseY) ?
                new Vector4f(0.5f, 0.5f, 0.5f, 1f)
                :
                new Vector4f(0.35f, 0.35f, 0.35f, 1f);
        Vector4f outlineColor = contains(mouseX, mouseY) ?
                new Vector4f(1f, 1f, 1f, 1f)
                :
                new Vector4f(1f, 1f, 1f, 0.3f);

        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(backGroundColor))
                .rectSized(this.x, this.y, this.width, this.height)
                .build()
                .tryDraw()
                .close();
        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(outlineColor))
                .rectSized(this.x, this.y, 1, this.height)
                .rectSized(this.x + 1, this.y, this.width - 1, 1)
                .rectSized(this.x + this.width - 1, this.y + 1, 1, this.height - 1)
                .rectSized(this.x + 1, this.y + this.height - 1, this.width - 2, 1)
                .build()
                .tryDraw()
                .close();
        new TextBatch(game.getRenderer().font)
                .text(new RenderText(
                        this.text,
                        this.x + (this.width - game.getRenderer().font.getTextWidth(this.text)) / 2,
                        this.y + (this.height - game.getRenderer().font.getTextHeight()) / 2
                ).withShadow(true))
                .build()
                .tryDraw()
                .close();
    }

    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (contains(mouseX, mouseY) && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            pressRunnable.run();
    }

    public boolean contains(double mouseX, double mouseY) {
        return this.aabb.contains(mouseX, mouseY);
    }
}
