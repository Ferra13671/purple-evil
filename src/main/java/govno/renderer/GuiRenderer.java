package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import com.ferra13671.cometrenderer.texture.GLTex;
import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import govno.GameInstance;
import govno.Settings;
import govno.renderer.batch.impl.BasicTextureBatch;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
import govno.world.LobbyWorld;
import govno.world.World;
import govno.world.attribute.Attributes;
import govno.world.attribute.modifier.AttributeInstance;
import govno.world.attribute.modifier.AttributeModifier;
import govno.world.entity.EnemyEntity;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import govno.world.upgrades.Upgrade;
import org.joml.Vector4f;

import java.util.*;

public class GuiRenderer implements GameInstance {
    private final Renderer renderer;

    public GuiRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render() {
        Framebuffer framebuffer = game.getWindow().getFramebuffer();
        int width = framebuffer.getWidth();
        int height = framebuffer.getHeight();
        this.renderer.projection.identity().ortho(
                0, width,
                height, 0,
                -100, 100
        );
        this.renderer.view.identity();

        framebuffer.bind(true);

        if (game.getGameSession() != null && game.getWorld() != null && !game.getGameSession().player.isDying())
            renderOverlay(framebuffer);

        if (game.getScreen() != null)
            game.getScreen().render(
                    game.getMouseListener().position.x,
                    game.getMouseListener().position.y
            );
    }

    private void renderOverlay(Framebuffer framebuffer) {
        renderStats(framebuffer);
        renderUpgrades(framebuffer);
        renderActionButtons(framebuffer);
        renderMinimap(framebuffer);
    }

    private void renderStats(Framebuffer framebuffer) {
        Player player = game.getGameSession().player;

        final float iconSize = 32f;
        final float gap = 8f;
        final float lineHeight = iconSize + gap;
        final float startX = 10f;
        final float startY = 10f;

        renderIcon(game.getTextures().healthTexture, startX, startY, iconSize);
        renderValueText(String.valueOf((int) player.getHealth()),
                startX + iconSize + gap, startY, iconSize);

        float scoreY = startY + lineHeight;
        renderIcon(game.getTextures().coinTexture, startX, scoreY, iconSize);
        renderValueText(String.valueOf(game.getGameSession().score),
                startX + iconSize + gap, scoreY, iconSize);

        float levelY = scoreY + lineHeight;
        new TextBatch(this.renderer.smallFont)
                .text(new RenderText("Level: " + game.getGameSession().currentLevel, startX, levelY)
                        .withShadow(true))
                .build().tryDraw().close();
    }

    private void renderIcon(GLTex texture, float x, float y, float size) {
        if (texture == null) return;

        new BasicTextureBatch()
                .texture(texture)
                .rectSized(x, y, size, size, new TextureBorder(0f, 0f, 1f, 1f))
                .build().tryDraw().close();
    }

    private void renderValueText(String value, float x, float iconY, float iconSize) {
        float textHeight = this.renderer.font.getTextHeight();
        float textY = iconY + (iconSize - textHeight) / 2f;

        new TextBatch(this.renderer.font)
                .text(new RenderText(value, x, textY).withShadow(true))
                .build().tryDraw().close();
    }

    private void renderMinimap(Framebuffer framebuffer) {
        World world = game.getWorld();
        if (world.rooms.isEmpty()) return;

        // --- 1. Вычисляем границы всего мира (всех комнат) ---
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Room room : world.rooms) {
            AABB b = room.getBounds();
            minX = Math.min(minX, b.getMinX());
            maxX = Math.max(maxX, b.getMaxX());
            minY = Math.min(minY, b.getMinY());
            maxY = Math.max(maxY, b.getMaxY());
        }

        float worldWidth = (float) (maxX - minX);
        float worldHeight = (float) (maxY - minY);
        float maxWorldDim = Math.max(worldWidth, worldHeight);

        float mapSize = 150f;
        float margin = 20f;
        float innerPadding = 12f;

        float mapX = framebuffer.getWidth() - mapSize - margin;
        float mapY = margin;

        float innerSize = mapSize - innerPadding * 2;
        float scale = innerSize / maxWorldDim;

        float offsetX = innerPadding + (innerSize - worldWidth * scale) / 2f;
        float offsetY = innerPadding + (innerSize - worldHeight * scale) / 2f;

        new ColoredRectBatch()
                .rectSized(mapX, mapY, mapSize, mapSize,
                        RectColors.verticalGradient(new Vector4f(0, 0, 0, 0.125f), new Vector4f(0, 0, 0, 0.125f)))
                .build().tryDraw().close();

        float borderThickness = 2f;
        Vector4f borderColor = new Vector4f(1f, 1f, 1f, 0.25f);
        new ColoredRectBatch()
                .rectSized(mapX, mapY, mapSize, borderThickness, RectColors.verticalGradient(borderColor, borderColor))
                .rectSized(mapX, mapY + mapSize - borderThickness, mapSize, borderThickness, RectColors.verticalGradient(borderColor, borderColor))
                .rectSized(mapX, mapY + borderThickness, borderThickness, mapSize - borderThickness * 2, RectColors.verticalGradient(borderColor, borderColor))
                .rectSized(mapX + mapSize - borderThickness, mapY + borderThickness, borderThickness, mapSize - borderThickness * 2, RectColors.verticalGradient(borderColor, borderColor))
                .build().tryDraw().close();

        ColoredRectBatch roomBatch = new ColoredRectBatch();
        Vector4f roomColor = new Vector4f(1f, 1f, 1f, 1f);

        for (Room room : world.rooms) {
            AABB b = room.getBounds();

            float rx = mapX + offsetX + (float) (b.getMinX() - minX) * scale;
            float ry = mapY + offsetY + (float) (maxY - b.getMaxY()) * scale;
            float rw = (float) (b.getMaxX() - b.getMinX()) * scale;
            float rh = (float) (b.getMaxY() - b.getMinY()) * scale;

            roomBatch.rectSized(rx, ry, rw, rh, RectColors.verticalGradient(roomColor, roomColor));
        }
        roomBatch.build().tryDraw().close();

        float pX = mapX + offsetX + (float) (game.getGameSession().player.position.x - minX) * scale;
        float pY = mapY + offsetY + (float) (maxY - game.getGameSession().player.position.y) * scale;
        float playerSize = 4f;

        new ColoredRectBatch()
                .rectSized(pX - playerSize / 2, pY - playerSize / 2, playerSize, playerSize,
                        RectColors.verticalGradient(new Vector4f(0, 0, 1, 1), new Vector4f(0, 0, 1, 1)))
                .build().tryDraw().close();

        ColoredRectBatch enemyBatch = new ColoredRectBatch();
        Vector4f enemyColor = new Vector4f(1f, 0f, 0f, 1f);

        for (Room room : world.rooms) {
            for (Entity entity : room.entities) {
                if (!(entity instanceof EnemyEntity enemy)) continue;
                if (enemy.removed || enemy.isDying()) continue;

                float ex = mapX + offsetX + (float) (enemy.position.x - minX) * scale;
                float ey = mapY + offsetY + (float) (maxY - enemy.position.y) * scale;

                double aabbSize = Math.max(
                        enemy.modelAABB.getXLength(),
                        enemy.modelAABB.getYLength()
                );
                float size = Math.clamp((float) (aabbSize * 2.5f), 3f, 6f);

                enemyBatch.rectSized(ex - size / 2, ey - size / 2, size, size,
                        RectColors.verticalGradient(enemyColor, enemyColor));
            }
        }
        enemyBatch.build().tryDraw().close();
    }

    private void renderUpgrades(Framebuffer framebuffer) {
        Player player = game.getGameSession().player;

        Map<String, Integer> counts = collectUpgradeCounts(player);
        if (counts.isEmpty()) return;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        final float iconSize = 40f;
        final float gap = 8f;
        final float margin = 20f;
        final int iconsPerRow = 8;

        int totalCount = entries.size();
        int rows = (totalCount + iconsPerRow - 1) / iconsPerRow;

        float rightEdge = framebuffer.getWidth() - margin;
        float bottomEdge = framebuffer.getHeight() - margin;
        float startY = bottomEdge - rows * iconSize - (rows - 1) * gap;

        for (int row = 0; row < rows; row++) {
            int rowStart = row * iconsPerRow;
            int rowEnd = Math.min(rowStart + iconsPerRow, totalCount);
            int rowCount = rowEnd - rowStart;

            float rowWidth = rowCount * iconSize + (rowCount - 1) * gap;
            float rowX = rightEdge - rowWidth;
            float rowY = startY + row * (iconSize + gap);

            for (int i = 0; i < rowCount; i++) {
                Map.Entry<String, Integer> entry = entries.get(rowStart + i);
                float x = rowX + i * (iconSize + gap);
                renderUpgradeIcon(entry.getKey(), x, rowY, iconSize);
                renderUpgradeCountBadge(entry.getValue(), x, rowY, iconSize);
            }
        }
    }

    private Map<String, Integer> collectUpgradeCounts(Player player) {
        Map<String, Integer> rawCounts = new HashMap<>();
        countModifiers(player.getAttributes(), rawCounts);

        if (player.getWeapon() != null)
            countModifiers(player.getWeapon().getAttributes(), rawCounts);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : rawCounts.entrySet()) {
            Upgrade u = game.getUpgrades().upgrades.get(e.getKey());
            if (u == null || u.modifiers().isEmpty())
                continue;

            int perApply = u.modifiers().size();
            int count = e.getValue() / perApply;
            if (count > 0)
                result.put(e.getKey(), count);
        }
        return result;
    }

    private void countModifiers(Attributes attributes, Map<String, Integer> target) {
        for (AttributeInstance instance : attributes.getAttributes().values()) {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (game.getUpgrades().upgrades.containsKey(modifier.id()))
                    target.merge(modifier.id(), 1, Integer::sum);
            }
        }
    }

    private void renderUpgradeIcon(String id, float x, float y, float size) {
        GLTexture texture = game.getTextures().getUpgradeTexture(id);
        if (texture == null)
            return;

        new BasicTextureBatch()
                .texture(game.getTextures().entityAtlas)
                .rectSized(x, y, size, size,
                        game.getTextures().entityAtlas.getBorder(texture))
                .build().tryDraw().close();
    }

    private void renderUpgradeCountBadge(int count, float iconX, float iconY, float iconSize) {
        if (count < 2)
            return;

        String text = "x" + count;
        float textWidth  = this.renderer.smallFont.getTextWidth(text);
        float textHeight = this.renderer.smallFont.getTextHeight();

        float x = iconX + iconSize - textWidth;
        float y = iconY + iconSize - textHeight;

        new TextBatch(this.renderer.smallFont)
                .text(new RenderText(text, x, y).withShadow(true))
                .build().tryDraw().close();
    }

    private void renderActionButtons(Framebuffer framebuffer) {
        final float size = 56f;
        final float gap = 12f;
        final float rowGap = 12f;
        final float margin = 20f;
        final float labelGap = 4f;

        float labelHeight = this.renderer.tinyFont.getTextHeight();
        float buttonBlockH = size + labelGap + labelHeight;
        float clusterH = 2 * size + gap + labelGap + labelHeight;

        float lowerY = framebuffer.getHeight() - margin - clusterH;
        float upperY = lowerY - rowGap - buttonBlockH;

        boolean inLobby = game.getWorld() instanceof LobbyWorld;

        float x = margin;
        renderActionButton(x, upperY, size, Settings.PAUSE.getDisplayName(), "Pause",
                0f, false, Settings.PAUSE.isPressed());
        x += size + gap;

        renderActionButton(x, upperY, size, Settings.PLAYER_SWAP.getDisplayName(), "Swap",
                game.getGameSession().getSwapCooldownProgress(), true, Settings.PLAYER_SWAP.isPressed());
        x += size + gap;

        renderActionButton(x, upperY, size, Settings.DASH.getDisplayName(), "Dash",
                game.getGameSession().player.getDashCooldownProgress(), true, Settings.DASH.isPressed());
        x += size + gap;

        if (inLobby) {
            renderActionButton(x, upperY, size, Settings.SHOP.getDisplayName(), "Shop",
                    0f, false, Settings.SHOP.isPressed());
        }

        float buttonswidth = 3 * size + 2 * gap;

        renderActionButton(margin + size + gap, lowerY, size, Settings.FORWARD.getDisplayName(),
                null, 0f, false, Settings.FORWARD.isPressed());

        float lowerRowY = lowerY + size + gap;
        renderActionButton(margin, lowerRowY, size, Settings.LEFT.getDisplayName(),
                null, 0f, false, Settings.LEFT.isPressed());
        renderActionButton(margin + size + gap, lowerRowY, size, Settings.BACK.getDisplayName(),
                null, 0f, false, Settings.BACK.isPressed());
        renderActionButton(margin + 2 * (size + gap), lowerRowY, size, Settings.RIGHT.getDisplayName(),
                null, 0f, false, Settings.RIGHT.isPressed());

        String movementText = "Movement";
        float movementTextWidth = this.renderer.tinyFont.getTextWidth(movementText);
        new TextBatch(this.renderer.tinyFont)
                .text(new RenderText(movementText,
                        margin + (buttonswidth - movementTextWidth) / 2f,
                        lowerRowY + size + labelGap).withShadow(true))
                .build().tryDraw().close();

        renderActionButton(margin + buttonswidth + gap * 2, lowerRowY, size, Settings.ATTACK.getDisplayName(),
                "Attack", 0f, false, Settings.ATTACK.isPressed());
    }

    private void renderActionButton(float x, float y, float size,
                                    String keyText,
                                    String name,
                                    float cooldownProgress,
                                    boolean showFill,
                                    boolean pressed) {
        Vector4f bg = new Vector4f(0.1f, 0.1f, 0.1f, 0.75f);
        new ColoredRectBatch()
                .rectSized(x, y, size, size, RectColors.verticalGradient(bg, bg))
                .build().tryDraw().close();

        if (showFill && cooldownProgress > 0.001f) {
            float fillHeight = size * Math.clamp(cooldownProgress, 0f, 1f);
            Vector4f fill = new Vector4f(1f, 1f, 1f, 0.4f);
            new ColoredRectBatch()
                    .rectSized(x, y, size, fillHeight, RectColors.verticalGradient(fill, fill))
                    .build().tryDraw().close();
        }

        if (pressed) {
            Vector4f glow = new Vector4f(0.4f, 0.7f, 1f, 0.35f);
            new ColoredRectBatch()
                    .rectSized(x, y, size, size, RectColors.verticalGradient(glow, glow))
                    .build().tryDraw().close();
        }

        float bt = 2f;
        Vector4f border = pressed
                ? new Vector4f(0.6f, 0.85f, 1f, 0.95f)
                : new Vector4f(1f, 1f, 1f, 0.55f);
        new ColoredRectBatch()
                .rectSized(x, y, size, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + size - bt, size, bt, RectColors.verticalGradient(border, border))
                .rectSized(x, y + bt, bt, size - bt * 2, RectColors.verticalGradient(border, border))
                .rectSized(x + size - bt, y + bt, bt, size - bt * 2, RectColors.verticalGradient(border, border))
                .build().tryDraw().close();

        float keyWidth  = this.renderer.smallFont.getTextWidth(keyText);
        float keyHeight = this.renderer.smallFont.getTextHeight();
        float keyX = x + (size - keyWidth) / 2f;
        float keyY = y + (size - keyHeight) / 2f;

        new TextBatch(this.renderer.smallFont)
                .text(new RenderText(keyText, keyX, keyY).withShadow(true))
                .build().tryDraw().close();

        if (name != null && !name.isEmpty()) {
            float nameWidth = this.renderer.tinyFont.getTextWidth(name);
            float nameX = x + (size - nameWidth) / 2f;
            float nameY = y + size + 4f;
            new TextBatch(this.renderer.tinyFont)
                    .text(new RenderText(name, nameX, nameY).withShadow(true))
                    .build().tryDraw().close();
        }
    }
}