package govno;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.FullScreenSwitchCombination;
import com.ferra13671.cometrenderer.plugins.glfw.Window;
import com.ferra13671.cometrenderer.plugins.glfw.WindowHints;
import govno.renderer.Programs;
import govno.renderer.ShaderLibraries;
import govno.renderer.Textures;
import govno.world.World;
import govno.world.room.RoomTemplates;
import lombok.Getter;
import govno.renderer.Renderer;

public class Game {
    @Getter
    private static Game instance;
    @Getter
    private final Ticker ticker = new Ticker(50);
    @Getter
    private Window window;
    @Getter
    private final RoomTemplates roomTemplates;
    @Getter
    private final World world;
    @Getter
    private final Textures textures;
    private final ShaderLibraries shaderLibraries;
    @Getter
    private final Programs programs;
    @Getter
    private final Renderer renderer;

    public Game() {
        instance = this;

        initWindow();
        CometRenderer.init();
        BetterCompilerPlugin.init();

        this.roomTemplates = new RoomTemplates();
        this.world = new World();

        this.textures = new Textures();
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
            this.ticker.onStartFrame();
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
