package govno.renderer.batch.impl.text;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.utils.Pair;
import govno.renderer.RectColors;
import govno.renderer.batch.IPrimitiveBatch;
import govno.renderer.batch.impl.ColoredTextureBatch;
import govno.renderer.font.Glyph;
import govno.renderer.font.TTFFont;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class TextBatch implements IPrimitiveBatch {
    private final Map<GLTexture, ColoredTextureBatch> batches = new HashMap<>();
    private final TTFFont font;
    private final Runnable preDrawRunnable;
    private boolean built = false;

    public TextBatch() {
        this((TTFFont) null);
    }

    public TextBatch(TTFFont font) {
        this(font, null);
    }

    public TextBatch(Runnable preDrawRunnable) {
        this(null, preDrawRunnable);
    }

    public TextBatch(TTFFont font, Runnable preDrawRunnable) {
        this.font = font;
        this.preDrawRunnable = preDrawRunnable;
    }

    public TextBatch text(RenderText text) {
        float x = text.getX();
        float y = text.getY();

        for (Pair<Supplier<Vector4fc>, Character[]> component : text.getText().getComponents()) {
            Vector4fc color = new Vector4f(text.getColor()).mul(component.left().get());

            for (char _char : component.right()) {
                Glyph glyph = text.getFont() != null ? text.getFont().getGlyph(_char) : this.font.getGlyph(_char);

                if (_char == '\n') {
                    x = text.getX();
                    y += glyph.height();
                }

                ColoredTextureBatch batch = this.batches.computeIfAbsent(glyph.instance().getTexture(), texture -> new ColoredTextureBatch().texture(texture));

                if (text.isShadow())
                    batch.rectSized(
                            x + 1,
                            y + 1,
                            glyph.width(),
                            glyph.height(),
                            RectColors.oneColor(new Vector4f(0f, 0f, 0f, 1f)),
                            glyph.border()
                    );
                batch.rectSized(
                        x,
                        y,
                        glyph.width(),
                        glyph.height(),
                        RectColors.oneColor(color),
                        glyph.border()
                );

                x += glyph.width();
            }
        }

        return this;
    }

    @Override
    public boolean isBuilt() {
        return this.built;
    }

    @Override
    public TextBatch build() {
        this.batches.forEach((_, batch) -> batch.build());
        this.built = true;
        return this;
    }

    @Override
    public TextBatch tryDraw() {
        if (this.preDrawRunnable != null)
            this.preDrawRunnable.run();

        this.batches.forEach((_, batch) -> batch.tryDraw());
        return this;
    }

    @Override
    public void close() {
        this.batches.forEach((_, batch) -> batch.close());
    }

    public TTFFont getFontOrNull() {
        return this.font;
    }
}
