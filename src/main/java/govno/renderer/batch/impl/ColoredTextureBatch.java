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
import com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder;
import govno.renderer.RectColors;
import govno.renderer.batch.AbstractPrimitiveBatch;
import org.joml.Matrix4f;

public class ColoredTextureBatch extends AbstractPrimitiveBatch {
    private UnitBindable textureBindable = TextureUnitBindable.EMPTY;
    private UnitBindable samplerBindable = SamplerUnitBindable.EMPTY;

    public ColoredTextureBatch(Runnable preDrawRunnable) {
        this();
        this.preDrawRunnable = preDrawRunnable;
    }

    public ColoredTextureBatch() {
        super(Mesh.builder(DrawMode.QUADS, VertexFormat.POSITION_COLOR_TEXTURE));
    }

    public ColoredTextureBatch(int allocatorSize, Runnable preDrawRunnable) {
        this(allocatorSize);
        this.preDrawRunnable = preDrawRunnable;
    }

    public ColoredTextureBatch(int allocatorSize) {
        super(Mesh.builder(allocatorSize, DrawMode.QUADS, VertexFormat.POSITION_COLOR_TEXTURE));
    }

    public ColoredTextureBatch texture(GLTex texture) {
        return texture(texture == null ? 0 : texture.getId());
    }

    public ColoredTextureBatch texture(int textureId) {
        return texture(textureId == 0 ? TextureUnitBindable.EMPTY : new TextureUnitBindable(textureId));
    }

    public ColoredTextureBatch texture(UnitBindable bindable) {
        this.textureBindable = bindable;

        return this;
    }

    public ColoredTextureBatch sampler(GLSampler sampler) {
        return sampler(sampler == null ? 0 : sampler.getId());
    }

    public ColoredTextureBatch sampler(int samplerId) {
        return sampler(samplerId == 0 ? SamplerUnitBindable.EMPTY : new SamplerUnitBindable(samplerId));
    }

    public ColoredTextureBatch sampler(UnitBindable bindable) {
        this.samplerBindable = bindable;

        return this;
    }

    public ColoredTextureBatch textureSampler(GLTex texture, GLSampler sampler) {
        return texture(texture).sampler(sampler);
    }

    public ColoredTextureBatch textureSampler(int[] ids) {
        return textureSampler(ids[0], ids[1]);
    }

    public ColoredTextureBatch textureSampler(UnitBindable[] bindables) {
        return textureSampler(bindables[0], bindables[1]);
    }

    public ColoredTextureBatch textureSampler(int textureId, int samplerId) {
        return texture(textureId).sampler(samplerId);
    }

    public ColoredTextureBatch textureSampler(UnitBindable textureBindable, UnitBindable samplerBindable) {
        return texture(textureBindable).sampler(samplerBindable);
    }

    public ColoredTextureBatch rectSized(float x, float y, float width, float height, RectColors rectColors, TextureBorder textureBorder) {
        return rectPositioned(x, y, x + width, y + height, rectColors, textureBorder, null);
    }

    public ColoredTextureBatch rectSized(float x, float y, float width, float height, RectColors rectColors, TextureBorder textureBorder, Matrix4f matrix4f) {
        return rectPositioned(x, y, x + width, y + height, rectColors, textureBorder, matrix4f);
    }

    public ColoredTextureBatch rectPositioned(float x1, float y1, float x2, float y2, RectColors rectColors, TextureBorder textureBorder) {
        return rectPositioned(x1, y1, x2, y2, rectColors, textureBorder, null);
    }

    public ColoredTextureBatch rectPositioned(float x1, float y1, float x2, float y2, RectColors rectColors, TextureBorder textureBorder, Matrix4f matrix4f) {
        vertex(x1, y1, matrix4f)
                .element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y1Color())
                .element("Texture", VertexElementType.FLOAT, textureBorder.u1(), textureBorder.v1());
        vertex(x1, y2, matrix4f)
                .element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y2Color())
                .element("Texture", VertexElementType.FLOAT, textureBorder.u1(), textureBorder.v2());
        vertex(x2, y2, matrix4f)
                .element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y2Color())
                .element("Texture", VertexElementType.FLOAT, textureBorder.u2(), textureBorder.v2());
        vertex(x2, y1, matrix4f)
                .element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y1Color())
                .element("Texture", VertexElementType.FLOAT, textureBorder.u2(), textureBorder.v1());

        return this;
    }

    private MeshBuilder vertex(float x, float y, Matrix4f matrix4f) {
        if (matrix4f != null)
            return this.meshBuilder.vertex(matrix4f, x, y, 0);
        else return this.meshBuilder.vertex(x, y, 0);
    }

    @Override
    protected void draw() {
        CometRenderer.setCurrentProgram(game.getPrograms().POSITION_COLOR_TEXTURE);

        CometRenderer.applyShaderColorUniform();
        game.getRenderer().applyMatrices();

        CometRenderer.getCurrentProgram().getSampler(0).setTextureSampler(this.textureBindable, this.samplerBindable);

        CometRenderer.draw(this.mesh, false);
    }
}
