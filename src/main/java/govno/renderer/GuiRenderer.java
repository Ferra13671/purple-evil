package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import govno.GameInstance;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
import govno.world.World;
import govno.world.entity.EnemyEntity;
import govno.world.entity.Entity;
import govno.world.entity.Player;
import govno.world.room.Room;
import org.joml.Vector4f;

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
        new TextBatch(this.renderer.font)
                .text(new RenderText(
                        String.format("Health: %s\nLevel: %s\nScore: %s", (int) game.getGameSession().player.getHealth(), game.getGameSession().currentLevel, game.getGameSession().score),
                        10,
                        10
                ).withShadow(true))
                .build()
                .tryDraw()
                .close();

        renderAttributes(framebuffer);

        renderMinimap(framebuffer);
    }

    private void renderAttributes(Framebuffer framebuffer) {
        StringBuilder builder = new StringBuilder();

        Player player = game.getGameSession().player;

        builder.append("Player attributes:\n");
        player.getAttributes().getAttributes().forEach((name, instance) ->
                builder.append(String.format("%s: %s\n", name, instance.getCurrentValue()))
        );
        builder.append("\nWeapon attributes:\n");
        if (player.getWeapon() != null)
            player.getWeapon().getAttributes().getAttributes().forEach((name, instance) ->
                    builder.append(String.format("%s: %s\n", name, instance.getCurrentValue()))
            );

        new TextBatch(this.renderer.smallFont)
                .text(new RenderText(
                        builder.toString(),
                        10,
                        framebuffer.getHeight() / 3f
                ).withShadow(true))
                .build()
                .tryDraw()
                .close();
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
}