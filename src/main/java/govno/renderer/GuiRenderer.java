package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import govno.GameInstance;
import govno.renderer.batch.impl.BasicRectBatch;

public class GuiRenderer implements GameInstance {
    private final Renderer renderer;

    public GuiRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render() {
        Framebuffer framebuffer = game.getWindow().getFramebuffer();
        int width = framebuffer.getWidth();
        int height = framebuffer.getHeight();
        this.renderer.projection.identity().ortho(
                0, width,
                height, 0,
                -100, 100
        );

        framebuffer.bind(true);

        new BasicRectBatch()
                .rectSized(500, 500, 100, 100)
                .build()
                .tryDraw()
                .close();

        if (game.getScreen() != null)
            game.getScreen().render(
                    game.getMouseListener().position.x,
                    game.getMouseListener().position.y
            );
    }
}
