package govno.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.glsl.uniform.UniformType;
import govno.GameInstance;
import govno.renderer.font.FontInfo;
import govno.renderer.font.TTFFont;
import org.joml.Matrix4f;

import java.awt.*;

public class Renderer implements GameInstance {
    public static boolean RENDER_WORLD = false;

    public Matrix4f projection = new Matrix4f();
    public Matrix4f view = new Matrix4f();

    protected final WorldRenderer worldRenderer = new WorldRenderer(this);
    protected final GuiRenderer guiRenderer = new GuiRenderer(this);

    public TTFFont font;
    {
        try {
            this.font = new TTFFont(
                    FontInfo.builder()
                            .font(Font.createFont(Font.TRUETYPE_FONT, GuiRenderer.class.getClassLoader().getResourceAsStream("assets/Classic-Console-Neue-Regular.ttf")))
                            .fontSize(32)
                            .build()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void render() {
        CometRenderer.setDefaultBlend();
        if (game.getWorld() != null) {
            Renderer.RENDER_WORLD = true;
            this.worldRenderer.render();
        }
        Renderer.RENDER_WORLD = false;
        this.guiRenderer.render();
    }

    public void applyMatrices() {
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("projection", UniformType.MATRIX4, u -> u.set(this.projection));
        CometRenderer.getCurrentProgram().consumeIfUniformPresent("view", UniformType.MATRIX4, u -> u.set(this.view));
    }
}
