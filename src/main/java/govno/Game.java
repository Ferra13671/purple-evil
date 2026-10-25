package govno;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.FullScreenSwitchCombination;
import com.ferra13671.cometrenderer.plugins.glfw.Window;
import com.ferra13671.cometrenderer.plugins.glfw.WindowHints;
import com.ferra13671.overtone.Overtone;
import govno.model.EntityModelTemplates;
import govno.renderer.Programs;
import govno.renderer.ShaderLibraries;
import govno.renderer.Textures;
import govno.screen.MainMenuScreen;
import govno.screen.Screen;
import govno.utils.KeyBind;
import govno.utils.Ticker;
import govno.world.World;
import govno.world.room.RoomTemplates;
import lombok.Getter;
import govno.renderer.Renderer;
import lombok.Setter;

public class Game {
    @Getter
    private static Game instance;
    @Getter
    private final Ticker ticker = new Ticker(50);
    @Getter
    private final MouseListener mouseListener = new MouseListener();
    @Getter
    private final KeyboardListener keyboardListener = new KeyboardListener();
    @Getter
    private Window window;
    @Getter
    private final RoomTemplates roomTemplates;
    @Getter
    private Screen screen;
    @Getter
    @Setter
    private World world;
    @Getter
    private final Textures textures;
    @Getter
    private final EntityModelTemplates entityModelTemplates;
    private final ShaderLibraries shaderLibraries;
    @Getter
    private final Programs programs;
    @Getter
    private final Renderer renderer;
    @Getter
    private final Sounds sounds;
    @Getter
    private final SoundManager soundManager;
    @Getter
    private final GameSession gameSession;

    public Game() {
        instance = this;

        initWindow();
        CometRenderer.init();
        BetterCompilerPlugin.init();

        this.roomTemplates = new RoomTemplates();

        this.textures = new Textures();
        this.entityModelTemplates = new EntityModelTemplates();
        this.shaderLibraries = new ShaderLibraries();
        this.programs = new Programs();
        this.renderer = new Renderer();

        this.textures.buildAtlases();

        Overtone.init();
        this.sounds = new Sounds();
        this.soundManager = new SoundManager();

        this.gameSession = new GameSession();

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

        this.window.setFramebufferSizeCallback((width, height) -> {
            if (this.screen != null)
                this.screen.resize(width, height);
        });
        this.window.setMouseCallback(this.mouseListener);
        this.window.setMousePosCallback(this.mouseListener);
        this.window.setKeyboardCallback(this.keyboardListener);

        this.window.setLoopCallback(() -> {
            this.ticker.onStartFrame();
            if (this.ticker.shouldTick()) {
                if (this.gameSession != null)
                    this.gameSession.tick();

                if (this.world != null)
                    this.world.tick();

                Overtone.tick();
                this.soundManager.tick();

                if (this.screen != null) {
                    this.screen.tick();
                    KeyBind.resetAll();
                } else if (this.world == null)
                    setScreen(new MainMenuScreen());

                this.ticker.reset();
            }

            this.renderer.render();
        });
    }

    public void setScreen(Screen screen) {
        if (this.screen != null)
            this.screen.onClose();

        this.screen = screen;
        if (this.screen != null)
            this.screen.onOpen(
                    this.window.getFramebufferWidth(),
                    this.window.getFramebufferHeight()
            );
    }

    public static void start() {
        new Game();
    }
}
