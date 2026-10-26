package govno.screen;

import com.ferra13671.cometrenderer.plugins.glfw.CometGLFW;
import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundSource;
import govno.GameSession;
import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen extends Screen {
    private SoundSource mainMenuSound;
    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);

        this.mainMenuSound = Overtone.create2DSource();
        this.mainMenuSound.setSoundBuffer(game.getSounds().MAIN_MENU);
        this.mainMenuSound.setLooping(true);
        this.mainMenuSound.play();
    }

    @Override
    public void onClose() {
        this.mainMenuSound.close();
        this.mainMenuSound = null;
    }

    @Override
    public void resize(int width, int height) {
        this.buttons.clear();

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float buttonWidth = 200;
        float buttonHeight = 50;
        float spacing = 15;

        float totalHeight = buttonHeight * 3 + spacing * 2;
        float startY = halfHeight - totalHeight / 2;

        this.buttons.add(new Button(
                "Play", halfWidth - (buttonWidth / 2), startY, buttonWidth, buttonHeight, () -> {
            game.setGameSession(new GameSession());
            game.getGameSession().startGame();
            game.setScreen(null);
        }
        ));
        this.buttons.add(new Button(
                "Settings", halfWidth - (buttonWidth / 2), startY + buttonHeight + spacing,
                buttonWidth, buttonHeight,
                () -> game.setScreen(new SettingsScreen(this))
        ));
        this.buttons.add(new Button(
                "Quit", halfWidth - (buttonWidth / 2), startY + (buttonHeight + spacing) * 2,
                buttonWidth, buttonHeight, CometGLFW::close
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        new ColoredRectBatch()
                .rectSized(0, 0, game.getWindow().getFramebufferWidth(), game.getWindow().getFramebufferHeight(), RectColors.verticalGradient(new Vector4f(0f, 0f, 0f, 1f), new Vector4f(0.6823f, 0, 1f, 1f)))
                .build()
                .tryDraw()
                .close();

        for (Button button : buttons)
            button.render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons)
            b.onMouseRelease(mouseX, mouseY, button);
    }
}