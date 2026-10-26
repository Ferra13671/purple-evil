package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.CometRenderer;
import stayablox.purpleevil.GameInstance;
import stayablox.purpleevil.renderer.batch.impl.BasicRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.utils.AABB;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class Slider implements GameInstance {
    private static final float TRACK_LEFT_OFFSET  = 200f;
    private static final float TRACK_RIGHT_OFFSET = 100f;
    private static final float TRACK_HEIGHT       = 6f;
    private static final float KNOB_SIZE          = 14f;
    private static final float PADDING            = 20f;

    private final String label;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;
    private final AABB aabb;

    private boolean dragging = false;

    public Slider(String label, float x, float y, float width, float height,
                  DoubleSupplier getter, DoubleConsumer setter) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.getter = getter;
        this.setter = setter;
        this.aabb = new AABB(x, y, x + width, y + height);
    }

    public void render(double mouseX, double mouseY) {
        boolean hovered = this.aabb.contains(mouseX, mouseY);
        boolean active = hovered || dragging;

        // Фон и рамка в стиле Button
        Vector4f bg = active ? new Vector4f(0.5f, 0.5f, 0.5f, 1f)
                             : new Vector4f(0.35f, 0.35f, 0.35f, 1f);
        Vector4f border = active ? new Vector4f(1f, 1f, 1f, 1f)
                                 : new Vector4f(1f, 1f, 1f, 0.3f);

        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(bg))
                .rectSized(this.x, this.y, this.width, this.height)
                .build().tryDraw().close();
        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(border))
                .rectSized(this.x, this.y, 1, this.height)
                .rectSized(this.x + 1, this.y, this.width - 1, 1)
                .rectSized(this.x + this.width - 1, this.y + 1, 1, this.height - 1)
                .rectSized(this.x + 1, this.y + this.height - 1, this.width - 2, 1)
                .build().tryDraw().close();

        // Название
        float textY = this.y + (this.height - game.getRenderer().smallFont.getTextHeight()) / 2f;
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(this.label, this.x + PADDING, textY).withShadow(true))
                .build().tryDraw().close();

        // Процент справа
        int percent = (int) Math.round(getter.getAsDouble() * 100);
        String valueText = percent + "%";
        float valueW = game.getRenderer().smallFont.getTextWidth(valueText);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(valueText, this.x + this.width - PADDING - valueW, textY).withShadow(true))
                .build().tryDraw().close();

        // Трек
        float trackLeft = this.x + TRACK_LEFT_OFFSET;
        float trackRight = this.x + this.width - TRACK_RIGHT_OFFSET;
        float trackW = trackRight - trackLeft;
        float trackY = this.y + (this.height - TRACK_HEIGHT) / 2f;

        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(new Vector4f(0.15f, 0.15f, 0.15f, 1f)))
                .rectSized(trackLeft, trackY, trackW, TRACK_HEIGHT)
                .build().tryDraw().close();

        double value = Math.clamp(getter.getAsDouble(), 0.0, 1.0);
        if (value > 0) {
            new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(new Vector4f(0.75f, 0.75f, 0.75f, 1f)))
                    .rectSized(trackLeft, trackY, trackW * (float) value, TRACK_HEIGHT)
                    .build().tryDraw().close();
        }

        // Knob — белый обычный / янтарный при драге
        float knobX = trackLeft + trackW * (float) value - KNOB_SIZE / 2f;
        float knobY = this.y + (this.height - KNOB_SIZE) / 2f;
        Vector4f knobColor = dragging
                ? new Vector4f(1f, 0.95f, 0.6f, 1f)
                : new Vector4f(1f, 1f, 1f, 1f);
        new BasicRectBatch(() -> CometRenderer.getShaderColor().setColor(knobColor))
                .rectSized(knobX, knobY, KNOB_SIZE, KNOB_SIZE)
                .build().tryDraw().close();
    }

    public void onMousePress(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.aabb.contains(mouseX, mouseY)) {
            this.dragging = true;
            updateFromMouse(mouseX);
        }
    }

    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        if (this.dragging) {
            this.dragging = false;
        } else if (this.aabb.contains(mouseX, mouseY)) {
            // Fallback: если игра не шлёт onMousePress — обрабатываем клик здесь
            updateFromMouse(mouseX);
        }
    }

    /** Вызывается каждый кадр в render(): если идёт драг — обновить значение по позиции мыши. */
    public void updateIfDragging(double mouseX) {
        if (this.dragging) updateFromMouse(mouseX);
    }

    public boolean contains(double mouseX, double mouseY) {
        return this.aabb.contains(mouseX, mouseY);
    }

    public boolean isDragging() {
        return dragging;
    }

    private void updateFromMouse(double mouseX) {
        float trackLeft = this.x + TRACK_LEFT_OFFSET;
        float trackRight = this.x + this.width - TRACK_RIGHT_OFFSET;
        float trackW = trackRight - trackLeft;
        if (trackW <= 0) return;
        double v = Math.clamp((mouseX - trackLeft) / trackW, 0.0, 1.0);
        this.setter.accept(v);
    }
}