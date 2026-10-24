package govno.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.uniform.UniformType;
import govno.GameInstance;
import org.joml.Matrix4f;

public class Renderer implements GameInstance {
    public static boolean RENDER_WORLD = false;

    public Matrix4f projection;
    public Matrix4f view = new Matrix4f();

    protected final WorldRenderer worldRenderer = new WorldRenderer(this);

    public void render() {
        this.worldRenderer.render();
    }

    public void applyMatrices() {
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("projection", UniformType.MATRIX4, u -> u.set(this.projection));
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("view", UniformType.MATRIX4, u -> u.set(this.view));
    }
}
