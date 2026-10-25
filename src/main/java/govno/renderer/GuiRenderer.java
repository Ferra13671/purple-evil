package govno.renderer;

import com.ferra13671.cometrenderer.buffer.framebuffer.Framebuffer;
import govno.GameInstance;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
import govno.world.World;
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

        if (game.getGameSession() != null && game.getWorld() != null)
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

        renderMinimap(framebuffer);
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

        // --- 2. Настройки миникарты ---
        float mapSize = 150f; // Размер миникарты в пикселях
        float margin = 20f;   // Отступ от края экрана
        float innerPadding = 12f; // <-- НОВЫЙ ОТСТУП: свободное место внутри рамки миникарты

        // Позиция в правом верхнем углу
        float mapX = framebuffer.getWidth() - mapSize - margin;
        float mapY = margin;

        // Вычисляем внутренний размер области для карты
        float innerSize = mapSize - innerPadding * 2;

        // Коэффициент масштабирования (считаем от внутреннего размера)
        float scale = innerSize / maxWorldDim;

        // Смещение для центрирования комнат внутри внутренней области
        float offsetX = innerPadding + (innerSize - worldWidth * scale) / 2f;
        float offsetY = innerPadding + (innerSize - worldHeight * scale) / 2f;

        // --- 3. Отрисовка фона и обводки ---
        // Фон (черный, прозрачность 1/8 = 0.125)
        new ColoredRectBatch()
                .rectSized(mapX, mapY, mapSize, mapSize,
                        RectColors.verticalGradient(new Vector4f(0, 0, 0, 0.125f), new Vector4f(0, 0, 0, 0.125f)))
                .build().tryDraw().close();

        // Обводка (белая, прозрачность 1/4 = 0.25)
        float borderThickness = 2f;
        Vector4f borderColor = new Vector4f(1f, 1f, 1f, 0.25f);
        new ColoredRectBatch()
                // Верхняя линия (на всю ширину)
                .rectSized(mapX, mapY, mapSize, borderThickness, RectColors.verticalGradient(borderColor, borderColor))
                // Нижняя линия (на всю ширину)
                .rectSized(mapX, mapY + mapSize - borderThickness, mapSize, borderThickness, RectColors.verticalGradient(borderColor, borderColor))
                // Левая линия (сдвинута вниз на толщину рамки и укорочена)
                .rectSized(mapX, mapY + borderThickness, borderThickness, mapSize - borderThickness * 2, RectColors.verticalGradient(borderColor, borderColor))
                // Правая линия (сдвинута вниз на толщину рамки и укорочена)
                .rectSized(mapX + mapSize - borderThickness, mapY + borderThickness, borderThickness, mapSize - borderThickness * 2, RectColors.verticalGradient(borderColor, borderColor))
                .build().tryDraw().close();

        // --- 4. Отрисовка комнат ---
        ColoredRectBatch roomBatch = new ColoredRectBatch();
        Vector4f roomColor = new Vector4f(1f, 1f, 1f, 1f); // Белый

        for (Room room : world.rooms) {
            AABB b = room.getBounds();

            float rx = mapX + offsetX + (float) (b.getMinX() - minX) * scale;
            float ry = mapY + offsetY + (float) (maxY - b.getMaxY()) * scale; // Инвертируем Y для экрана
            float rw = (float) (b.getMaxX() - b.getMinX()) * scale;
            float rh = (float) (b.getMaxY() - b.getMinY()) * scale;

            roomBatch.rectSized(rx, ry, rw, rh, RectColors.verticalGradient(roomColor, roomColor));
        }
        roomBatch.build().tryDraw().close();

        // --- 5. Отрисовка игрока (синий квадрат) ---
        float pX = mapX + offsetX + (float) (game.getGameSession().player.position.x - minX) * scale;
        float pY = mapY + offsetY + (float) (maxY - game.getGameSession().player.position.y) * scale;
        float playerSize = 4f; // Размер точки на миникарте

        new ColoredRectBatch()
                .rectSized(pX - playerSize / 2, pY - playerSize / 2, playerSize, playerSize,
                        RectColors.verticalGradient(new Vector4f(0, 0, 1, 1), new Vector4f(0, 0, 1, 1)))
                .build().tryDraw().close();
    }
}
