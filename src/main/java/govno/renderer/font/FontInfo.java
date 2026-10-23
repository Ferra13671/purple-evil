package govno.renderer.font;

import lombok.Builder;
import lombok.Getter;

import java.awt.*;

@Builder
@Getter
public class FontInfo {
    private final Font font;
    @Builder.Default
    private int glyphsInMap = 500;
    @Builder.Default
    private boolean antiAliasing = true;
    @Builder.Default
    private boolean smoothFiltering = true;
    private final int fontSize;
    @Builder.Default
    private final FontType fontType = FontType.Plain;
}
