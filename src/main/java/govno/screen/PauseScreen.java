package govno.screen;

import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class PauseScreen extends Screen {
    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        this.buttons.clear();

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float buttonWidth = 250;
        float buttonHeight = 50;
        float spacing = 15;

        float firstButtonY = halfHeight + 20;

        this.buttons.add(new Button(
                "Resume",
                halfWidth - (buttonWidth / 2), firstButtonY, buttonWidth, buttonHeight,
                () -> game.setScreen(null)
        ));

        this.buttons.add(new Button(
                "Restart",
                halfWidth - (buttonWidth / 2), firstButtonY + (buttonHeight + spacing), buttonWidth, buttonHeight,
                () -> {
                    game.setScreen(new ConfirmScreen(
                            this,
                            "RESTART?",
                            "Are you sure you want to restart?",
                            () -> {
                                game.getGameSession().loadCheckpoint();
                                game.getGameSession().startLobby();
                                game.setScreen(null);
                            }
                    ));
                }
        ));

        this.buttons.add(new Button(
                "Main Menu",
                halfWidth - (buttonWidth / 2), firstButtonY + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight,
                () -> {
                    game.setWorld(null);
                    game.setGameSession(null);
                    game.setScreen(new MainMenuScreen());
                }
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        new ColoredRectBatch()
                .rectSized(0, 0, game.getWindow().getFramebufferWidth(), game.getWindow().getFramebufferHeight(),
                        RectColors.verticalGradient(
                                new Vector4f(0, 0, 0, 0.6f),
                                new Vector4f(0, 0, 0, 0.6f)))
                .build().tryDraw().close();

        String text = "PAUSED";
        float textWidth = game.getRenderer().bigFont.getTextWidth(text);
        float textHeight = game.getRenderer().bigFont.getTextHeight();
        float textX = (game.getWindow().getFramebufferWidth() / 2f) - (textWidth / 2f);
        float textY = (game.getWindow().getFramebufferHeight() / 2f) - textHeight - 60;

        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(text, textX, textY).withShadow(true))
                .build().tryDraw().close();

        for (Button button : buttons)
            button.render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons)
            b.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}