package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.world.upgrades.Upgrade;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShopScreen extends Screen {
    private static final int COLS = 4;
    private static final int ROWS = 2;
    private static final int PER_PAGE = COLS * ROWS;

    private static final float CARDS_GAP = 20f;

    private final List<Upgrade> allUpgrades = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final List<UpgradeCard> pageCards = new ArrayList<>();
    private final List<Upgrade> pageUpgrades = new ArrayList<>();
    private int page = 0;
    private int totalPages = 1;

    @Override
    public void onOpen(int width, int height) {
        allUpgrades.clear();
        game.getUpgrades().upgrades.values().stream()
                .sorted(Comparator.comparing(Upgrade::name))
                .forEach(allUpgrades::add);
        this.page = 0;
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();
        totalPages = Math.max(1, (allUpgrades.size() + PER_PAGE - 1) / PER_PAGE);
        if (page >= totalPages) page = totalPages - 1;
        computePage();

        float buttonW = 200f;
        float buttonH = 50f;
        float bottomY = height - buttonH - 30f;
        float centerX = width / 2f;

        buttons.add(new Button(
                "Close", centerX - buttonW / 2f, bottomY, buttonW, buttonH,
                () -> game.setScreen(null)
        ));

        if (totalPages > 1) {
            buttons.add(new Button(
                    "< Prev", 30, bottomY, 130, buttonH,
                    () -> {
                        if (page > 0) { page--; computePage(); }
                    }
            ));
            buttons.add(new Button(
                    "Next >", width - 160, bottomY, 130, buttonH,
                    () -> {
                        if (page < totalPages - 1) { page++; computePage(); }
                    }
            ));
        }

        int count = pageUpgrades.size();
        int cols = Math.min(COLS, count);
        int rows = (count + COLS - 1) / COLS;

        float gridW = cols * UpgradeCard.CARD_WIDTH + (cols - 1) * CARDS_GAP;
        float gridH = rows * UpgradeCard.CARD_HEIGHT + (rows - 1) * CARDS_GAP;
        float gridX = (width - gridW) / 2f;
        float gridY = (height - gridH) / 2f - 30f;

        for (int i = 0; i < count; i++) {
            int col = i % COLS;
            int row = i / COLS;
            float cardX = gridX + col * (UpgradeCard.CARD_WIDTH + CARDS_GAP);
            float cardY = gridY + row * (UpgradeCard.CARD_HEIGHT + CARDS_GAP);
            pageCards.add(new UpgradeCard(
                    pageUpgrades.get(i),
                    cardX, cardY
            ));
        }
    }

    private void computePage() {
        pageUpgrades.clear();
        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, allUpgrades.size());
        for (int i = start; i < end; i++) pageUpgrades.add(allUpgrades.get(i));
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        renderBackground();

        String title = "SHOP";
        float titleWidth = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleWidth) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        renderCoins();

        renderCards(width, height, mouseX, mouseY);

        for (Button b : buttons) b.render(mouseX, mouseY);

        if (totalPages > 1) {
            String p = (page + 1) + " / " + totalPages;
            float pW = game.getRenderer().smallFont.getTextWidth(p);
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(p, (width - pW) / 2f, height - 40f).withShadow(true))
                    .build().tryDraw().close();
        }
    }

    private void renderCoins() {
        final float iconSize = 52f;
        final float gap = 12f;
        final float margin = 24f;

        String scoreText = String.valueOf(game.getGameSession().score);
        float textHeight = game.getRenderer().bigFont.getTextHeight();

        float startX = margin;
        float startY = margin;

        if (game.getTextures().coinTexture != null) {
            new BasicTextureBatch()
                    .texture(game.getTextures().coinTexture)
                    .rectSized(startX, startY, iconSize, iconSize,
                            new TextureBorder(0f, 0f, 1f, 1f))
                    .build().tryDraw().close();
        }

        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(
                        scoreText,
                        startX + iconSize + gap,
                        startY + (iconSize - textHeight) / 2f
                ).withShadow(true))
                .build().tryDraw().close();
    }

    private void renderCards(int width, int height, double mouseX, double mouseY) {

        if (pageUpgrades.isEmpty()) {
            String empty = "No upgrades available";
            float w = game.getRenderer().font.getTextWidth(empty);
            new TextBatch(game.getRenderer().font)
                    .text(new RenderText(empty, (width - w) / 2f, height / 2f - 20f).withShadow(true))
                    .build().tryDraw().close();
            return;
        }

        for (UpgradeCard card : pageCards)
            card.render(mouseX, mouseY);
    }

    @Override
    public void onMousePress(double mouseX, double mouseY, int button) {
        for (UpgradeCard card : pageCards)
            card.onMousePress(mouseX, mouseY, button);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (UpgradeCard card : pageCards)
            card.onMouseRelease(mouseX, mouseY, button);

        for (Button b : buttons)
            b.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}