package stayablox.purpleevil.screen;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.world.upgrades.Upgrade;

import java.util.concurrent.ThreadLocalRandom;

public class UpgradeCard extends Button {
    public static final float CARD_WIDTH = 170f;
    public static final float CARD_HEIGHT = 210f;
    public static final int CARD_CLICK_ANIM_DURATION = 200;
    public static final float CARD_CLICK_ANIM_SIZE_DELTA = 0.75f;

    private final Upgrade upgrade;

    private boolean pressed = false;
    private long releaseTime = 0;

    public UpgradeCard(Upgrade upgrade, float x, float y) {
        super("", x, y, CARD_WIDTH, CARD_HEIGHT, () -> {});
        this.upgrade = upgrade;
    }

    @Override
    public void render(double mouseX, double mouseY) {
        long currentTime = System.currentTimeMillis();
        boolean hovered = contains(mouseX, mouseY);

        if (this.pressed && !hovered) {
            this.pressed = false;
            this.releaseTime = currentTime;
        }

        float scale = 1.0f;

        if (this.pressed) {
            scale = CARD_CLICK_ANIM_SIZE_DELTA;
            this.releaseTime = 0;
        } else if (this.releaseTime > 0) {
            long elapsed = currentTime - this.releaseTime;
            if (elapsed < CARD_CLICK_ANIM_DURATION) {
                float progress = (float) elapsed / CARD_CLICK_ANIM_DURATION;
                scale = CARD_CLICK_ANIM_SIZE_DELTA + (1.0f - CARD_CLICK_ANIM_SIZE_DELTA) * progress;
            } else {
                this.releaseTime = 0;
                scale = 1.0f;
            }
        }

        int price = game.getGameSession().getCurrentPrice(upgrade);
        boolean affordable = game.getGameSession().score >= price;

        float width = CARD_WIDTH * scale;
        float height = CARD_HEIGHT * scale;
        float x = this.x + (this.width - width) / 2f;
        float y = this.y + (this.height - height) / 2f;

        Vector4f bg = affordable
                ? (hovered ? new Vector4f(0.2f, 0.35f, 0.2f, 1f) : new Vector4f(0.1f, 0.2f, 0.1f, 1f))
                : new Vector4f(0.2f, 0.1f, 0.1f, 1f);
        Vector4f border = hovered
                ? new Vector4f(1f, 1f, 1f, 1f)
                : new Vector4f(1f, 1f, 1f, 0.3f);

        new ColoredRectBatch()
                .rectSized(x, y, width, height, RectColors.verticalGradient(bg, bg))
                .build().tryDraw().close();

        float bt = 2f * scale;
        new ColoredRectBatch()
                .rectSized(x, y, width, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + height - bt, width, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + bt, bt, height - bt * 2, RectColors.verticalGradient(border, border))
                .rectSized(x + width - bt, y + bt, bt, height - bt * 2, RectColors.verticalGradient(border, border))
                .build().tryDraw().close();

        GLTexture iconTex = game.getTextures().getUpgradeTexture(upgrade.name());
        float iconSize = 80f * scale;
        float iconX = x + (width - iconSize) / 2f;
        float iconY = y + 15f * scale;

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
                .text(new RenderText(name, x + (width - nameW) / 2f, y + 105f * scale).withShadow(true))
                .build().tryDraw().close();

        String priceText = String.valueOf(price);
        float priceTextW = game.getRenderer().font.getTextWidth(priceText);
        float priceIconSize = 28f * scale;
        float totalW = priceIconSize + 6f * scale + priceTextW;
        float priceStartX = x + (width - totalW) / 2f;
        float priceY = y + height - 45f * scale;

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
                        priceStartX + priceIconSize + 6f * scale,
                        priceY + (priceIconSize - game.getRenderer().font.getTextHeight()) / 2f)
                        .withShadow(true).withColor(priceColor))
                .build().tryDraw().close();

        int purchased = game.getGameSession().getUpgradePurchaseCount(upgrade.name());
        if (purchased > 0) {
            String owned = "x" + purchased;
            float ownedW = game.getRenderer().smallFont.getTextWidth(owned);
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(owned, x + width - ownedW - 8f * scale, y + 6f * scale).withShadow(true))
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
                            x + (width - hintW) / 2f,
                            y + height - 20f * scale).withShadow(true).withColor(hintColor))
                    .build().tryDraw().close();
        }
    }

    public void onMousePress(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && contains(mouseX, mouseY)) {
            int price = game.getGameSession().getCurrentPrice(upgrade);
            if (game.getGameSession().score >= price) {
                this.pressed = true;
            }
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.pressed) {
                this.pressed = false;
                this.releaseTime = System.currentTimeMillis();

                if (contains(mouseX, mouseY)) {
                    int price = game.getGameSession().getCurrentPrice(upgrade);
                    if (game.getGameSession().score >= price) {
                        if (game.getGameSession().purchaseUpgrade(upgrade)) {
                            game.getSoundManager().playSound(game.getSounds().BUY, 0.3f, ThreadLocalRandom.current().nextFloat(1f, 1.5f));
                        }
                    }
                }
            }
        }
    }
}