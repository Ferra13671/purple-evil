package govno;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Ticker {
    @Getter
    private final int tickDelay;
    private long lastTickTime = System.currentTimeMillis();
    private long frameTime = 0;

    public void onStartFrame() {
        this.frameTime = System.currentTimeMillis();
    }

    public boolean shouldTick() {
        return System.currentTimeMillis() - this.lastTickTime >= this.tickDelay;
    }

    public float getTickDelta() {
        return (this.frameTime - this.lastTickTime) / (float) this.tickDelay;
    }

    public void reset() {
        this.lastTickTime = System.currentTimeMillis();
    }
}
