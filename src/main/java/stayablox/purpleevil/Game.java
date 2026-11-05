package stayablox.purpleevil;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.CometTags;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.*;
import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundBuffer;
import com.ferra13671.overtone.SoundSource;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import stayablox.purpleevil.model.EntityModelTemplates;
import stayablox.purpleevil.renderer.Programs;
import stayablox.purpleevil.renderer.ShaderLibraries;
import stayablox.purpleevil.renderer.Textures;
import stayablox.purpleevil.screen.MainMenuScreen;
import stayablox.purpleevil.screen.PauseScreen;
import stayablox.purpleevil.screen.Screen;
import stayablox.purpleevil.utils.FPSCounter;
import stayablox.purpleevil.utils.KeyBind;
import stayablox.purpleevil.utils.Ticker;
import stayablox.purpleevil.world.LobbyWorld;
import stayablox.purpleevil.world.World;
import stayablox.purpleevil.world.attribute.AttributesTemplates;
import stayablox.purpleevil.world.dungeon.DungeonWorld;
import stayablox.purpleevil.world.room.RoomTemplates;
import stayablox.purpleevil.world.upgrades.Upgrades;
import lombok.Getter;
import stayablox.purpleevil.renderer.Renderer;
import lombok.Setter;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

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
    private final FPSCounter fpsCounter = new FPSCounter(1000);
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
    @Getter
    private long initTime;

    // Поля для управления фоновой музыкой
    private SoundSource currentMusic;
    private SoundBuffer currentMusicBuffer;

    public Game() {
        instance = this;

        System.out.println("Start initialize game");
        long time = System.currentTimeMillis();

        initWindow();
        CometRenderer.init();
        BetterCompilerPlugin.init();
        System.out.printf("Initialized CometRenderer v%s%n", CometRenderer.getRegistry().get(CometTags.COMET_RENDERER_VERSION).orElse("UNKNOWN"));

        this.roomTemplates = new RoomTemplates();
        System.out.printf("Loaded room templates: %s%n", this.roomTemplates.templates.size());
        this.attributesTemplates = new AttributesTemplates();
        this.upgrades = new Upgrades();
        System.out.printf("Loaded upgrades: %s%n", this.upgrades.upgrades.size());

        this.textures = new Textures();
        System.out.printf("Loaded textures: %s%n", CometRenderer.getTexturesCount());
        this.entityModelTemplates = new EntityModelTemplates();
        new ShaderLibraries();
        this.programs = new Programs();
        System.out.printf("Compiled programs: %s%n", CometRenderer.getProgramsCount());
        this.renderer = new Renderer();

        this.textures.buildAtlases();

        System.out.println("Initialize Overtone");
        Overtone.init();
        this.sounds = new Sounds();
        this.soundManager = new SoundManager();
        System.out.printf("Loaded sounds: %s%n", Overtone.getBuffers().size());

        this.initTime = System.currentTimeMillis();
        System.out.printf("Game initialized! Initialization time: %s%n", this.initTime - time);

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
        setWindowIcon();
        this.window.setSize(1300, 750);
        this.window.setMonitorCenterPosition();
        this.window.setVisible(true);
        CometGLFW.setVSync(Settings.VSYNC ? VsyncMode.Adaptive : VsyncMode.Disabled);

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
                    if (this.world == null) {
                        setScreen(new MainMenuScreen());
                    } else if (Settings.PAUSE.isPressed()) {
                        Settings.PAUSE.setPressed(false);
                        setScreen(new PauseScreen());
                    }
                }

                // Обновляем музыку каждый тик
                updateMusic();

                this.ticker.reset();
            }

            this.renderer.render();
            this.fpsCounter.onEndFrame();
        });
    }

    private void setWindowIcon() {
        ByteBuffer iconBuffer;
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("window-icon.png")) {
            if (in == null) {
                System.err.println("window-icon.png not found");
                return;
            }
            byte[] bytes = in.readAllBytes();
            iconBuffer = ByteBuffer.allocateDirect(bytes.length);
            iconBuffer.put(bytes).flip();
        } catch (Exception e) {
            System.err.println("Failed to read window-icon.png: " + e.getMessage());
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer pixels = STBImage.stbi_load_from_memory(iconBuffer, w, h, channels, 4);
            if (pixels == null) {
                System.err.println("Failed to decode icon: " + STBImage.stbi_failure_reason());
                return;
            }

            GLFWImage img = GLFWImage.malloc();
            try {
                img.set(w.get(0), h.get(0), pixels);
                this.window.setIcon(img);
            } finally {
                img.free();
                STBImage.stbi_image_free(pixels);
            }
        }
    }

    private void updateMusic() {
        SoundBuffer desiredBuffer = null;

        if (this.world instanceof LobbyWorld) {
            desiredBuffer = this.sounds.LOBBY;
        } else if (this.world instanceof DungeonWorld) {
            desiredBuffer = this.sounds.DUNGEON;
        } else if (this.world == null && this.gameSession == null) {
            desiredBuffer = this.sounds.MAIN_MENU;
        }

        if (desiredBuffer != this.currentMusicBuffer) {
            if (this.currentMusic != null) {
                this.currentMusic.close();
                this.currentMusic = null;
            }

            this.currentMusicBuffer = desiredBuffer;

            if (this.currentMusicBuffer != null) {
                this.currentMusic = Overtone.create2DSource();
                this.currentMusic.setSoundBuffer(this.currentMusicBuffer);
                this.currentMusic.setLooping(true);
                this.currentMusic.setVolume(Settings.MUSIC_VOLUME);
                this.currentMusic.play();
            }
        } else if (this.currentMusic != null)
            this.currentMusic.setVolume(Settings.MUSIC_VOLUME * 0.7f);
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