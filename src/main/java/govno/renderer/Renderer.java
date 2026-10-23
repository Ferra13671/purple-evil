package govno.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.uniform.UniformType;
import govno.GameInstance;
import govno.renderer.batch.impl.BasicRectBatch;
import org.joml.Matrix4f;

public class Renderer implements GameInstance {
    public Matrix4f projection;
    public Matrix4f view = new Matrix4f();

    public void render() {
        float unitsWide = 2;
        float aspect    = (float) GameInstance.game.getWindow().getFramebufferWidth() / GameInstance.game.getWindow().getFramebufferHeight();
        float halfW = unitsWide * 0.5f;
        float halfH = halfW / aspect;

        this.projection = new Matrix4f().ortho(
                -halfW, halfW,
                -halfH, halfH,
                -100, 100
        );

        game.getWindow().getFramebuffer().bind(true);

        new BasicRectBatch()
                .rectSized(0, 0, 1, 1)
                .build()
                .tryDraw()
                .close();
    }

    public void applyMatrices() {
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("projection", UniformType.MATRIX4, u -> u.set(this.projection));
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("view", UniformType.MATRIX4, u -> u.set(this.view));
    }
}
