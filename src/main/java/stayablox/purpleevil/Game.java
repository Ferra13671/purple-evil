package stayablox.purpleevil;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.plugins.bettercompiler.BetterCompilerPlugin;
import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.cometrenderer.plugins.glfw.FullScreenSwitchCombination;
import com.ferra13671.cometrenderer.plugins.glfw.Window;
import com.ferra13671.cometrenderer.plugins.glfw.WindowHints;
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

    // Поля для управления фоновой музыкой
    private SoundSource currentMusic;
    private SoundBuffer currentMusicBuffer;

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
        setWindowIcon();
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
                this.currentMusic.setVolume(Settings.SOUNDS_VOLUME);
                this.currentMusic.play();
            }
        } else if (this.currentMusic != null)
            this.currentMusic.setVolume(Settings.SOUNDS_VOLUME * 0.7f);
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