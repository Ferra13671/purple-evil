package stayablox.purpleevil.renderer.font;

import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;

public record Glyph(char symbol, TextureBorder border, float width, float height, GlyphMap instance) {
}
