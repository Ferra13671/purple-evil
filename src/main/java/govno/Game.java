package govno;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.FullScreenSwitchCombination;
import com.ferra13671.cometrenderer.plugins.glfw.Window;
import com.ferra13671.cometrenderer.plugins.glfw.WindowHints;
import govno.renderer.Programs;
import govno.renderer.ShaderLibraries;
import govno.world.World;
import lombok.Getter;
import govno.renderer.Renderer;

public class Game {
    @Getter
    private static Game instance;
    @Getter
    private final Ticker ticker = new Ticker(50);
    @Getter
    private Window window;
    private final ShaderLibraries shaderLibraries;
    @Getter
    private final Programs programs;
    @Getter
    private final Renderer renderer;
    @Getter
    private final World world = new World();

    public Game() {
        instance = this;

        initWindow();
        CometRenderer.init();
        BetterCompilerPlugin.init();

        this.shaderLibraries = new ShaderLibraries();
        this.programs = new Programs();
        this.renderer = new Renderer();

        this.window.loop();
    }

    public void initWindow() {
        CometGLFW.init(
                WindowHints.builder().build(),
                FullScreenSwitchCombination.F11,
                false
        );

        this.window = CometGLFW.getWindow();
        this.window.setTitle("govno.Game");
        this.window.setVisible(true);

        this.window.setLoopCallback(() -> {
            if (this.ticker.shouldTick()) {
                this.world.tick();

                this.ticker.reset();
            }

            this.renderer.render();
        });
    }

    public static void start() {
        new Game();
    }
}
