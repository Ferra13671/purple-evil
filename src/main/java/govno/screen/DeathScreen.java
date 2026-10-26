package govno.screen;

import govno.GameSession;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;

import java.util.ArrayList;
import java.util.List;

public class DeathScreen extends Screen {
    private long openTime;
    private final List<Button> buttons = new ArrayList<>();

    @Override
    public void onOpen(int width, int height) {
        if (this.openTime == 0)
            this.openTime = System.currentTimeMillis();
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

        this.buttons.add(new Button(
                "Restart",
                halfWidth - (buttonWidth / 2), halfHeight + 20, buttonWidth, buttonHeight, () -> {
            game.setScreen(new ConfirmScreen(
                    this,
                    "RESTART?",
                    "Are you sure you want to restart?",
                    () -> {
                        game.setGameSession(new GameSession());
                        game.getGameSession().startGame();
                        game.setScreen(null);
                    }
            ));
        }
        ));

        this.buttons.add(new Button(
                "Continue",
                halfWidth - (buttonWidth / 2), halfHeight + 20 + buttonHeight + spacing, buttonWidth, buttonHeight, () -> {
            game.getGameSession().loadCheckpoint();
            game.getGameSession().startLobby();
            game.setScreen(null);
        }
        ));

        this.buttons.add(new Button(
                "Main Menu",
                halfWidth - (buttonWidth / 2), halfHeight + 20 + (buttonHeight + spacing) * 2, buttonWidth, buttonHeight, () -> {
            game.setWorld(null);
            game.setGameSession(null);
            game.setScreen(new MainMenuScreen());
        }
        ));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        long elapsed = System.currentTimeMillis() - openTime;
        float bgAlpha = Math.min(1.0f, elapsed / 3000f);

        renderBackground(bgAlpha);

        if (elapsed > 1000) {
            String text = "GAME OVER";
            float textWidth = game.getRenderer().bigFont.getTextWidth(text);
            float textHeight = game.getRenderer().bigFont.getTextHeight();
            float textX = (game.getWindow().getFramebufferWidth() / 2f) - (textWidth / 2f);
            float textY = (game.getWindow().getFramebufferHeight() / 2f) - textHeight - 60;

            new TextBatch(game.getRenderer().bigFont)
                    .text(new RenderText(text, textX, textY)
                            .withShadow(true))
                    .build().tryDraw().close();
        }

        if (elapsed > 1400) this.buttons.get(0).render(mouseX, mouseY);
        if (elapsed > 1700) this.buttons.get(1).render(mouseX, mouseY);
        if (elapsed > 2000) this.buttons.get(2).render(mouseX, mouseY);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        long elapsed = System.currentTimeMillis() - openTime;
        
        if (elapsed > 1400) this.buttons.get(0).onMouseRelease(mouseX, mouseY, button);
        if (elapsed > 1700) this.buttons.get(1).onMouseRelease(mouseX, mouseY, button);
        if (elapsed > 2000) this.buttons.get(2).onMouseRelease(mouseX, mouseY, button);
    }
}