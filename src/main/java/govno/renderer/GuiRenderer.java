package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import com.ferra13671.cometrenderer.texture.GLTex;
import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.atlas.TextureBorder;
import govno.GameInstance;
import govno.renderer.batch.impl.BasicTextureBatch;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
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
}