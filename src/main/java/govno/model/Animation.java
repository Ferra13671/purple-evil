package govno.model;

import java.util.List;

public record Animation(String name, boolean loop, List<Frame> frames) {

    public record Frame(Sprite sprite, float duration) {}

    public float totalDuration() {
        float sum = 0f;
        for (Frame f : frames())
            sum += f.duration();

        return sum;
    }
}