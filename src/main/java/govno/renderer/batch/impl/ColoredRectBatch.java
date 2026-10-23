package govno.renderer.batch.impl;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.vertex.DrawMode;
import com.ferra13671.cometrenderer.vertex.element.VertexElementType;
import com.ferra13671.cometrenderer.vertex.format.VertexFormat;
import com.ferra13671.cometrenderer.vertex.mesh.Mesh;
import govno.renderer.RectColors;
import govno.renderer.batch.AbstractPrimitiveBatch;
import org.joml.Matrix4f;

public class ColoredRectBatch extends AbstractPrimitiveBatch {

    public ColoredRectBatch(Runnable preDrawRunnable) {
        this();
        this.preDrawRunnable = preDrawRunnable;
    }

    public ColoredRectBatch() {
        super(Mesh.builder(DrawMode.QUADS, VertexFormat.POSITION_COLOR));
    }

    public ColoredRectBatch(int allocatorSize, Runnable preDrawRunnable) {
        this(allocatorSize);
        this.preDrawRunnable = preDrawRunnable;
    }

    public ColoredRectBatch(int allocatorSize) {
        super(Mesh.builder(allocatorSize, DrawMode.QUADS, VertexFormat.POSITION_COLOR));
    }

    public ColoredRectBatch rectSized(float x, float y, float width, float height, RectColors rectColors) {
        return rectPositioned(x, y, x + width, y + height, rectColors);
    }

    public ColoredRectBatch rectSized(float x, float y, float width, float height, RectColors rectColors, Matrix4f matrix4f) {
        return rectPositioned(x, y, x + width, y + height, rectColors, matrix4f);
    }

    public ColoredRectBatch rectPositioned(float x1, float y1, float x2, float y2, RectColors rectColors) {
        this.meshBuilder.vertex(x1, y1, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y1Color());
        this.meshBuilder.vertex(x1, y2, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y2Color());
        this.meshBuilder.vertex(x2, y2, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y2Color());
        this.meshBuilder.vertex(x2, y1, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y1Color());

        return this;
    }

    public ColoredRectBatch rectPositioned(float x1, float y1, float x2, float y2, RectColors rectColors, Matrix4f matrix4f) {
        this.meshBuilder.vertex(matrix4f, x1, y1, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y1Color());
        this.meshBuilder.vertex(matrix4f, x1, y2, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x1y2Color());
        this.meshBuilder.vertex(matrix4f, x2, y2, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y2Color());
        this.meshBuilder.vertex(matrix4f, x2, y1, 0).element("Color", VertexElementType.VECTOR_4_FLOAT, rectColors.x2y1Color());

        return this;
    }

    @Override
    protected void draw() {
        CometRenderer.setCurrentProgram(game.getPrograms().POSITION_COLOR);

        CometRenderer.applyShaderColorUniform();
        game.getRenderer().applyMatrices();

        CometRenderer.draw(this.mesh, false);
    }
}
