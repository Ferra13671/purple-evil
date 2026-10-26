package stayablox.purpleevil;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.FullScreenSwitchCombination;
import com.ferra13671.cometrenderer.plugins.glfw.Window;
import com.ferra13671.cometrenderer.plugins.glfw.WindowHints;
import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundSource;
import stayablox.purpleevil.model.EntityModelTemplates;
import stayablox.purpleevil.renderer.Programs;
import stayablox.purpleevil.renderer.ShaderLibraries;
import stayablox.purpleevil.renderer.Textures;
import stayablox.purpleevil.screen.MainMenuScreen;
import stayablox.purpleevil.screen.PauseScreen;
import stayablox.purpleevil.screen.Screen;
import stayablox.purpleevil.utils.KeyBind;
import stayablox.purpleevil.utils.Ticker;
import stayablox.purpleevil.world.World;
import stayablox.purpleevil.world.attribute.AttributesTemplates;
import stayablox.purpleevil.world.room.RoomTemplates;
import stayablox.purpleevil.world.upgrades.Upgrades;
import lombok.Getter;
import stayablox.purpleevil.renderer.Renderer;
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
    private final AttributesTemplates attributesTemplates;
    @Getter
    private final Upgrades upgrades;
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
    @Setter
    private GameSession gameSession;
    private SoundSource menuMusic;

    public Game() {
        instance = this;

        initWindow();
        CometRenderer.init();
        BetterCompilerPlugin.init();

        this.roomTemplates = new RoomTemplates();
        this.attributesTemplates = new AttributesTemplates();
        this.upgrades = new Upgrades();

        this.textures = new Textures();
        this.entityModelTemplates = new EntityModelTemplates();
        this.shaderLibraries = new ShaderLibraries();
        this.programs = new Programs();
        this.renderer = new Renderer();

        this.textures.buildAtlases();

        Overtone.init();
        this.sounds = new Sounds();
        this.soundManager = new SoundManager();

        this.window.loop();
    }

    public void initWindow() {
        CometGLFW.init(
                WindowHints.builder().build(),
                FullScreenSwitchCombination.F11,
                false
        );

        this.window = CometGLFW.getWindow();
        this.window.setTitle("Purple Evil");
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
                if (!isGamePaused()) {
                    if (this.gameSession != null)
                        this.gameSession.tick();

                    if (this.world != null)
                        this.world.tick();
                }

                Overtone.tick();
                this.soundManager.tick();

                if (this.screen != null) {
                    this.screen.tick();
                    KeyBind.resetAll();
                } else {
                    if (this.world == null)
                        setScreen(new MainMenuScreen());
                    else if (Settings.PAUSE.isPressed()) {
                        Settings.PAUSE.setPressed(false);
                        setScreen(new PauseScreen());
                    }
                }

                updateMenuMusic();

                this.ticker.reset();
            }

            this.renderer.render();
        });
    }

    private void updateMenuMusic() {
        boolean shouldPlay = (this.world == null && this.gameSession == null);

        if (shouldPlay && this.menuMusic == null) {
            this.menuMusic = Overtone.create2DSource();
            this.menuMusic.setSoundBuffer(this.sounds.MAIN_MENU);
            this.menuMusic.setLooping(true);
            this.menuMusic.play();
        } else if (!shouldPlay && this.menuMusic != null) {
            this.menuMusic.close();
            this.menuMusic = null;
        }

        if (this.menuMusic != null) {
            this.menuMusic.setVolume(Settings.SOUNDS_VOLUME);
        }
    }

    public boolean isGamePaused() {
        return this.screen != null && this.screen.shouldPause();
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
