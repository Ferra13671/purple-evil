package govno.renderer.batch;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.vertex.mesh.Mesh;
import com.ferra13671.cometrenderer.vertex.mesh.MeshBuilder;
import govno.GameInstance;

public abstract class AbstractPrimitiveBatch implements IPrimitiveBatch, GameInstance {
    protected Runnable preDrawRunnable = () -> {};
    protected final MeshBuilder meshBuilder;
    protected Mesh mesh;
    protected boolean built = false;
    protected boolean closed = false;

    public AbstractPrimitiveBatch(MeshBuilder meshBuilder) {
        this.meshBuilder = meshBuilder;
    }

    @Override
    public IPrimitiveBatch tryDraw() {
        assertBuilt();
        assertNotClosed();

        if (this.mesh != null) {
            CometRenderer.getShaderColor().push();
            this.preDrawRunnable.run();

            draw();

            CometRenderer.getShaderColor().pop();
        }

        return this;
    }

    protected abstract void draw();

    @Override
    public boolean isBuilt() {
        return this.built;
    }

    @Override
    public IPrimitiveBatch build() {
        assertNotBuilt();
        assertNotClosed();

        this.mesh = this.meshBuilder.buildNullable();
        this.built = true;

        return this;
    }

    protected void assertBuilt() {
        if (!this.built)
            throw new IllegalStateException("Batch not built");
    }

    protected void assertNotBuilt() {
        if (this.built)
            throw new IllegalStateException("Batch already built");
    }

    protected void assertNotClosed() {
        if (this.closed)
            throw new IllegalStateException("Batch already closed");
    }

    @Override
    public void close() {
        if (!this.closed) {

            if (this.mesh != null)
                this.mesh.close();
            this.closed = true;
        }
    }
}
