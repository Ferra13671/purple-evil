package govno.renderer;

import govno.AABB;
import govno.GameInstance;
import govno.renderer.batch.impl.BasicRectBatch;
import org.joml.Matrix4f;

public class WorldRenderer implements GameInstance {
    private final Renderer renderer;

    public WorldRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render() {
        float unitsWide = 30;
        float aspect    = (float) GameInstance.game.getWindow().getFramebufferWidth() / GameInstance.game.getWindow().getFramebufferHeight();
        float halfW = unitsWide * 0.5f;
        float halfH = halfW / aspect;

        this.renderer.projection = new Matrix4f().ortho(
                -halfW, halfW,
                -halfH, halfH,
                -100, 100
        );

        game.getWindow().getFramebuffer().bind(true);

        AABB aabb = game.getWorld().player.worldAABB;
        new BasicRectBatch()
                .rectPositioned((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY())
                .build()
                .tryDraw()
                .close();
    }
}
