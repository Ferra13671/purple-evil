package govno.renderer;

import org.joml.Vector4fc;

public record RectColors(Vector4fc x1y1Color, Vector4fc x1y2Color, Vector4fc x2y2Color, Vector4fc x2y1Color) {

    public static RectColors oneColor(Vector4fc color) {
        return new RectColors(color, color, color, color);
    }

    public static RectColors horizontalGradient(Vector4fc left, Vector4fc right) {
        return new RectColors(left, left, right, right);
    }

    public static RectColors verticalGradient(Vector4fc up, Vector4fc down) {
        return new RectColors(up, down, down, up);
    }
}
