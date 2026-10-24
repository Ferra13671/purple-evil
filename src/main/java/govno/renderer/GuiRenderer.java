package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import govno.GameInstance;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;

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
        this.renderer.view.identity();

        framebuffer.bind(true);

        if (game.getWorld() != null)
            renderOverlay(framebuffer);

        if (game.getScreen() != null)
            game.getScreen().render(
                    game.getMouseListener().position.x,
                    game.getMouseListener().position.y
            );
    }

    private void renderOverlay(Framebuffer framebuffer) {
        new TextBatch(this.renderer.font)
                .text(new RenderText(
                        String.format("Health: %s\nLevel: %s", (int) game.getGameSession().player.getHealth(), game.getGameSession().currentLevel),
                        10,
                        10
                ).withShadow(true))
                .build()
                .tryDraw()
                .close();
    }
}
