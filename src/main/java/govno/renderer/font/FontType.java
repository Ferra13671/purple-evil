package govno.renderer.font;

import lombok.AllArgsConstructor;

import java.awt.*;

@AllArgsConstructor
public enum FontType {
    Plain(Font.PLAIN),
    Bold(Font.BOLD),
    Italic(Font.ITALIC);

    public final int id;
}
