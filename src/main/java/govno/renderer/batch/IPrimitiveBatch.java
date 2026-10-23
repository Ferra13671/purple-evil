package govno.renderer.batch;

public interface IPrimitiveBatch extends AutoCloseable {

    IPrimitiveBatch tryDraw();

    boolean isBuilt();

    IPrimitiveBatch build();

    @Override
    void close();
}
