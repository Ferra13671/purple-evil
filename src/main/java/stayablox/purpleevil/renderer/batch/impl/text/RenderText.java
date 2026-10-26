package stayablox.purpleevil.renderer.batch.impl.text;

import stayablox.purpleevil.renderer.font.FormattedText;
import stayablox.purpleevil.renderer.font.TTFFont;
import lombok.Getter;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Getter
public class RenderText {
    private FormattedText text;
    private float x;
    private float y;
    private Vector4fc color = new Vector4f(1f, 1f, 1f, 1f);
    private boolean shadow = false;
    private TTFFont font;

    public RenderText(String s, float x, float y) {
        this(new FormattedText(s), x, y);
    }

    public RenderText(FormattedText text, float x, float y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public RenderText withText(String s) {
        return withText(new FormattedText(s));
    }

    public RenderText withText(FormattedText text) {
        this.text = text;
        return this;
    }

    public RenderText withX(float x) {
        this.x = x;
        return this;
    }

    public RenderText withY(float y) {
        this.y = y;
        return this;
    }

    public RenderText withColor(Vector4fc color) {
        this.color = color;
        return this;
    }

    public RenderText withShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public RenderText withFont(TTFFont font) {
        this.font = font;
        return this;
    }
}
