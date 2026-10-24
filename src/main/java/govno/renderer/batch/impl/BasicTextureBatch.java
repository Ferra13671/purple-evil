package govno.renderer.batch.impl;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.sampler.GLSampler;
import com.ferra13671.cometrenderer.sampler.unit.SamplerUnitBindable;
import com.ferra13671.cometrenderer.sampler.unit.TextureUnitBindable;
import com.ferra13671.cometrenderer.sampler.unit.UnitBindable;
import com.ferra13671.cometrenderer.texture.GLTex;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import com.ferra13671.cometrenderer.vertex.DrawMode;
import com.ferra13671.cometrenderer.vertex.element.VertexElementType;
import com.ferra13671.cometrenderer.vertex.format.VertexFormat;
import com.ferra13671.cometrenderer.vertex.mesh.Mesh;
import govno.renderer.Renderer;
import govno.renderer.batch.AbstractPrimitiveBatch;
import org.joml.Matrix4f;

public class BasicTextureBatch extends AbstractPrimitiveBatch {
    private UnitBindable textureBindable = TextureUnitBindable.EMPTY;
    private UnitBindable samplerBindable = SamplerUnitBindable.EMPTY;

    public BasicTextureBatch(Runnable preDrawRunnable) {
        this();
        this.preDrawRunnable = preDrawRunnable;
    }

    public BasicTextureBatch() {
        super(Mesh.builder(DrawMode.QUADS, VertexFormat.POSITION_TEXTURE));
    }

    public BasicTextureBatch(int allocatorSize, Runnable preDrawRunnable) {
        this(allocatorSize);
        this.preDrawRunnable = preDrawRunnable;
    }

    public BasicTextureBatch(int allocatorSize) {
        super(Mesh.builder(allocatorSize, DrawMode.QUADS, VertexFormat.POSITION_TEXTURE));
    }

    public BasicTextureBatch texture(GLTex texture) {
        return texture(texture == null ? 0 : texture.getId());
    }

    public BasicTextureBatch texture(int textureId) {
        return texture(textureId == 0 ? TextureUnitBindable.EMPTY : new TextureUnitBindable(textureId));
    }

    public BasicTextureBatch texture(UnitBindable bindable) {
        this.textureBindable = bindable;

        return this;
    }

    public BasicTextureBatch sampler(GLSampler sampler) {
        return sampler(sampler == null ? 0 : sampler.getId());
    }

    public BasicTextureBatch sampler(int samplerId) {
        return sampler(samplerId == 0 ? SamplerUnitBindable.EMPTY : new SamplerUnitBindable(samplerId));
    }

    public BasicTextureBatch sampler(UnitBindable bindable) {
        this.samplerBindable = bindable;

        return this;
    }

    public BasicTextureBatch textureSampler(GLTex texture, GLSampler sampler) {
        return texture(texture).sampler(sampler);
    }

    public BasicTextureBatch textureSampler(int[] ids) {
        return textureSampler(ids[0], ids[1]);
    }

    public BasicTextureBatch textureSampler(UnitBindable[] bindables) {
        return textureSampler(bindables[0], bindables[1]);
    }

    public BasicTextureBatch textureSampler(int textureId, int samplerId) {
        return texture(textureId).sampler(samplerId);
    }

    public BasicTextureBatch textureSampler(UnitBindable textureBindable, UnitBindable samplerBindable) {
        return texture(textureBindable).sampler(samplerBindable);
    }

    public BasicTextureBatch rectSized(float x, float y, float width, float height, TextureBorder textureBorder) {
        return rectPositioned(x, y, x + width, y + height, textureBorder);
    }

    public BasicTextureBatch rectSized(float x, float y, float width, float height, TextureBorder textureBorder, Matrix4f matrix4f) {
        return rectPositioned(x, y, x + width, y + height, textureBorder, matrix4f);
    }

    public BasicTextureBatch rectPositioned(float x1, float y1, float x2, float y2, TextureBorder textureBorder) {
        this.meshBuilder.vertex(x1, y1, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u1(), Renderer.RENDER_WORLD ? textureBorder.v2() : textureBorder.v1());
        this.meshBuilder.vertex(x1, y2, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u1(), Renderer.RENDER_WORLD ? textureBorder.v1() : textureBorder.v2());
        this.meshBuilder.vertex(x2, y2, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u2(), Renderer.RENDER_WORLD ? textureBorder.v1() : textureBorder.v2());
        this.meshBuilder.vertex(x2, y1, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u2(), Renderer.RENDER_WORLD ? textureBorder.v2() : textureBorder.v1());

        return this;
    }

    public BasicTextureBatch rectPositioned(float x1, float y1, float x2, float y2, TextureBorder textureBorder, Matrix4f matrix4f) {
        this.meshBuilder.vertex(matrix4f, x1, y1, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u1(), Renderer.RENDER_WORLD ? textureBorder.v2() : textureBorder.v1());
        this.meshBuilder.vertex(matrix4f, x1, y2, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u1(), Renderer.RENDER_WORLD ? textureBorder.v1() : textureBorder.v2());
        this.meshBuilder.vertex(matrix4f, x2, y2, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u2(), Renderer.RENDER_WORLD ? textureBorder.v1() : textureBorder.v2());
        this.meshBuilder.vertex(matrix4f, x2, y1, 0).element("Texture", VertexElementType.FLOAT, textureBorder.u2(), Renderer.RENDER_WORLD ? textureBorder.v2() : textureBorder.v1());

        return this;
    }

    @Override
    protected void draw() {
        CometRenderer.setCurrentProgram(game.getPrograms().POSITION_TEXTURE);

        CometRenderer.applyShaderColorUniform();
        game.getRenderer().applyMatrices();

        CometRenderer.getCurrentProgram().getSampler(0).setTextureSampler(this.textureBindable, this.samplerBindable);

        CometRenderer.draw(this.mesh, false);
    }
}
