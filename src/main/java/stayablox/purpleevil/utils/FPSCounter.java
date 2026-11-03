package stayablox.purpleevil.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FPSCounter {
    private int framesCount;
    private long lastUpdate = -1;
    private final long updateDelay;

    @Getter
    private int currentFPS = -1;
    @Getter
    private int maxFPS = Integer.MIN_VALUE;
    @Getter
    private int minFPS = Integer.MAX_VALUE;

    public void onEndFrame() {
        this.framesCount++;

        long millis = System.currentTimeMillis();

        if (this.lastUpdate == -1) {
            this.lastUpdate = millis;
            return;
        }

        if (millis - this.lastUpdate > this.updateDelay) {
            this.currentFPS = this.framesCount;

            if (this.currentFPS > this.maxFPS)
                this.maxFPS = this.currentFPS;
            if (this.currentFPS < this.minFPS)
                this.minFPS = this.currentFPS;

            this.framesCount = 0;

            this.lastUpdate = millis;
        }
    }
}
