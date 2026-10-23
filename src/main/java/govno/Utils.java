package govno;

import lombok.experimental.UtilityClass;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2f;
import org.joml.Vector2fc;

@UtilityClass
public class Utils {

    public float lerp(float start, float end, float step) {
        return start + step * (end - start);
    }

    public double lerp(double start, double end, double step) {
        return start + step * (end - start);
    }

    public Vector2f lerp(Vector2fc start, Vector2fc end, float step) {
        return new Vector2f(
                lerp(start.x(), end.x(), step),
                lerp(start.y(), end.y(), step)
        );
    }

    public Vector2d lerp(Vector2dc start, Vector2dc end, float step) {
        return new Vector2d(
                lerp(start.x(), end.x(), step),
                lerp(start.y(), end.y(), step)
        );
    }
}
