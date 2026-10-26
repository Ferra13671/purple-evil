package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.upgrades.Upgrade;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShopScreen extends Screen {
    private static final int COLS = 4;
    private static final int ROWS = 2;
    private static final int PER_PAGE = COLS * ROWS;

    private static final float CARD_W = 170f;
    private static final float CARD_H = 210f;
    private static final float CARD_GAP = 20f;

    private final List<Upgrade> allUpgrades = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final List<AABB> cardAABBs = new ArrayList<>();
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
        cardAABBs.clear();

        if (pageUpgrades.isEmpty()) {
            String empty = "No upgrades available";
            float w = game.getRenderer().font.getTextWidth(empty);
            new TextBatch(game.getRenderer().font)
                    .text(new RenderText(empty, (width - w) / 2f, height / 2f - 20f).withShadow(true))
                    .build().tryDraw().close();
            return;
        }

        int count = pageUpgrades.size();
        int cols = Math.min(COLS, count);
        int rows = (count + COLS - 1) / COLS;

        float gridW = cols * CARD_W + (cols - 1) * CARD_GAP;
        float gridH = rows * CARD_H + (rows - 1) * CARD_GAP;
        float gridX = (width - gridW) / 2f;
        float gridY = (height - gridH) / 2f - 30f;

        for (int i = 0; i < count; i++) {
            int col = i % COLS;
            int row = i / COLS;
            float cardX = gridX + col * (CARD_W + CARD_GAP);
            float cardY = gridY + row * (CARD_H + CARD_GAP);
            renderCard(pageUpgrades.get(i), cardX, cardY, mouseX, mouseY);
        }
    }

    private void renderCard(Upgrade upgrade, float x, float y, double mouseX, double mouseY) {
        AABB aabb = new AABB(x, y, x + CARD_W, y + CARD_H);
        cardAABBs.add(aabb);

        int price = game.getGameSession().getCurrentPrice(upgrade);
        boolean affordable = game.getGameSession().score >= price;
        boolean hovered = aabb.contains(mouseX, mouseY);

        Vector4f bg = affordable
                ? (hovered ? new Vector4f(0.2f, 0.35f, 0.2f, 1f) : new Vector4f(0.1f, 0.2f, 0.1f, 1f))
                : new Vector4f(0.2f, 0.1f, 0.1f, 1f);
        Vector4f border = hovered
                ? new Vector4f(1f, 1f, 1f, 1f)
                : new Vector4f(1f, 1f, 1f, 0.3f);

        new ColoredRectBatch()
                .rectSized(x, y, CARD_W, CARD_H, RectColors.verticalGradient(bg, bg))
                .build().tryDraw().close();

        float bt = 2f;
        new ColoredRectBatch()
                .rectSized(x, y, CARD_W, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + CARD_H - bt, CARD_W, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + bt, bt, CARD_H - bt * 2, RectColors.verticalGradient(border, border))
                .rectSized(x + CARD_W - bt, y + bt, bt, CARD_H - bt * 2, RectColors.verticalGradient(border, border))
                .build().tryDraw().close();

        GLTexture iconTex = game.getTextures().getUpgradeTexture(upgrade.name());
        float iconSize = 80f;
        float iconX = x + (CARD_W - iconSize) / 2f;
        float iconY = y + 15f;
        if (iconTex != null) {
            new BasicTextureBatch()
                    .texture(game.getTextures().entityAtlas)
                    .rectSized(iconX, iconY, iconSize, iconSize,
                            game.getTextures().entityAtlas.getBorder(iconTex))
                    .build().tryDraw().close();
        }

        String name = upgrade.name();
        float nameW = game.getRenderer().smallFont.getTextWidth(name);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(name, x + (CARD_W - nameW) / 2f, y + iconSize + 25f).withShadow(true))
                .build().tryDraw().close();

        String priceText = String.valueOf(price);
        float priceTextW = game.getRenderer().font.getTextWidth(priceText);
        float priceIconSize = 28f;
        float totalW = priceIconSize + 6f + priceTextW;
        float priceStartX = x + (CARD_W - totalW) / 2f;
        float priceY = y + CARD_H - 55f;

        if (game.getTextures().coinTexture != null) {
            new BasicTextureBatch()
                    .texture(game.getTextures().coinTexture)
                    .rectSized(priceStartX, priceY, priceIconSize, priceIconSize,
                            new TextureBorder(0f, 0f, 1f, 1f))
                    .build().tryDraw().close();
        }
        Vector4f priceColor = affordable
                ? new Vector4f(1f, 1f, 1f, 1f)
                : new Vector4f(1f, 0.4f, 0.4f, 1f);
        new TextBatch(game.getRenderer().font)
                .text(new RenderText(priceText,
                        priceStartX + priceIconSize + 6f,
                        priceY + (priceIconSize - game.getRenderer().font.getTextHeight()) / 2f)
                        .withShadow(true).withColor(priceColor))
                .build().tryDraw().close();

        int purchased = game.getGameSession().getUpgradePurchaseCount(upgrade.name());
        if (purchased > 0) {
            String owned = "x" + purchased;
            float ownedW = game.getRenderer().smallFont.getTextWidth(owned);
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(owned, x + CARD_W - ownedW - 8f, y + 6f).withShadow(true))
                    .build().tryDraw().close();
        }

        if (hovered) {
            String hint = affordable ? "Click to buy" : "Not enough coins";
            float hintW = game.getRenderer().tinyFont.getTextWidth(hint);
            Vector4f hintColor = affordable
                    ? new Vector4f(0.7f, 1f, 0.7f, 1f)
                    : new Vector4f(1f, 0.6f, 0.6f, 1f);
            new TextBatch(game.getRenderer().tinyFont)
                    .text(new RenderText(hint,
                            x + (CARD_W - hintW) / 2f,
                            y + CARD_H - 24f).withShadow(true).withColor(hintColor))
                    .build().tryDraw().close();
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < cardAABBs.size(); i++) {
                if (cardAABBs.get(i).contains(mouseX, mouseY)) {
                    game.getGameSession().purchaseUpgrade(pageUpgrades.get(i));
                    return;
                }
            }
        }
        for (Button b : buttons) b.onMouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}