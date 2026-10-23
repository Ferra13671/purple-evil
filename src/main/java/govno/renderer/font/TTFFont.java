package govno.renderer.font;

import com.ferra13671.cometrenderer.utils.Pair;
import lombok.Getter;
import org.joml.Vector4fc;

import java.awt.*;
import java.util.HashMap;
import java.util.function.Supplier;

public class TTFFont {
    @Getter
    private final Font font;
    @Getter
    private final int glyphsInMap;
    @Getter
    private final boolean antiAliasing;
    @Getter
    private final boolean smoothFiltering;
    private final HashMap<Integer, GlyphMap> glyphMaps = new HashMap<>();

    public TTFFont(FontInfo fontInfo) {
        this.font = fontInfo.getFont().deriveFont(fontInfo.getFontType().id, fontInfo.getFontSize());
        this.glyphsInMap = fontInfo.getGlyphsInMap();
        this.antiAliasing = fontInfo.isAntiAliasing();
        this.smoothFiltering = fontInfo.isSmoothFiltering();
    }

    public Glyph getGlyph(char _char) {
        GlyphMap glyphMap = this.glyphMaps.computeIfAbsent(_char - (_char % this.glyphsInMap), index -> new GlyphMap(this, index));

        return glyphMap.getGlyph(_char);
    }

    public float getTextHeight() {
        return getGlyph('|').height();
    }

    public float getTextHeight(String s) {
        return getTextHeight() * (s.chars().filter(ch -> ch == '\n').count() + 1);
    }

    public float getTextWidth(String s) {
        return getTextWidth(new FormattedText(s));
    }

    public float getTextWidth(FormattedText text) {
        float maxW = 0;
        float w = 0;

        for (Pair<Supplier<Vector4fc>, Character[]> component : text.getComponents()) {
            for (char _char : component.right()) {
                if (_char == '\n') {
                    maxW = Math.max(maxW, w);
                    w = 0;

                    continue;
                }

                w += getGlyph(_char).width();
            }
        }

        return Math.max(maxW, w);
    }
}
