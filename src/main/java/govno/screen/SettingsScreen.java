package govno.screen;

import govno.Settings;
import govno.renderer.RectColors;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.renderer.batch.impl.text.RenderText;
import govno.renderer.batch.impl.text.TextBatch;
import govno.utils.AABB;
import govno.utils.KeyBind;
import govno.utils.KeyType;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private static final float ROW_H = 44f;
    private static final float ROW_GAP = 6f;
    private static final float ROW_W = 500f;
    private static final float PADDING = 20f;

    private final List<Slider> sliders = new ArrayList<>();
    private float bindsStartY;

    private final Screen previousScreen;
    private final List<KeyBind> binds = new ArrayList<>();
    private final List<String> bindLabels = new ArrayList<>();
    private final List<AABB> rowAABBs = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();

    private KeyBind capturing = null;

    public SettingsScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void onOpen(int width, int height) {
        binds.clear();
        bindLabels.clear();
        for (Field f : Settings.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            if (f.getType() != KeyBind.class) continue;
            try {
                binds.add((KeyBind) f.get(null));
                bindLabels.add(prettify(f.getName()));
            } catch (IllegalAccessException ignored) {}
        }
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();
        sliders.clear();

        float btnW = 200f;
        float btnH = 50f;
        float bottomY = height - btnH - 30f;
        float centerX = width / 2f;
        buttons.add(new Button("Back", centerX - btnW / 2f, bottomY, btnW, btnH,
                () -> game.setScreen(previousScreen)));

        // Раскладка: [slider block] + gap + [binds block]
        int count = binds.size();
        float bindsBlockH = count > 0 ? count * ROW_H + (count - 1) * ROW_GAP : 0f;
        float sliderBlockH = ROW_H;
        float sectionGap = 20f;
        float totalH = sliderBlockH + sectionGap + bindsBlockH;

        float startY = (height - totalH) / 2f - 30f;
        float startX = (width - ROW_W) / 2f;

        sliders.add(new Slider(
                "Sounds Volume", startX, startY, ROW_W, ROW_H,
                () -> Settings.SOUNDS_VOLUME,
                v -> Settings.SOUNDS_VOLUME = (float) v
        ));

        this.bindsStartY = startY + sliderBlockH + sectionGap;
    }

    @Override
    public void render(double mouseX, double mouseY) {
        // Обновляем драг слайдера ДО отрисовки — чтобы knob попал в актуальную позицию
        for (Slider s : sliders) s.updateIfDragging(mouseX);

        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        new ColoredRectBatch()
                .rectSized(0, 0, width, height,
                        RectColors.verticalGradient(
                                new Vector4f(0, 0, 0, 0.75f),
                                new Vector4f(0, 0, 0, 0.75f)))
                .build().tryDraw().close();

        String title = "SETTINGS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        for (Slider s : sliders) s.render(mouseX, mouseY);

        renderBinds(width, height, mouseX, mouseY);

        for (Button b : buttons) b.render(mouseX, mouseY);

        String hint = capturing != null
                ? "Press any key or click to rebind, ESC to cancel"
                : "Click a keybind to rebind";
        float hintW = game.getRenderer().smallFont.getTextWidth(hint);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(hint, (width - hintW) / 2f, height - 100f).withShadow(true))
                .build().tryDraw().close();
    }

    private void renderBinds(int width, int height, double mouseX, double mouseY) {
        rowAABBs.clear();
        int count = binds.size();
        if (count == 0) return;

        float startY = this.bindsStartY;
        float startX = (width - ROW_W) / 2f;

        for (int i = 0; i < count; i++) {
            KeyBind bind = binds.get(i);
            float rowY = startY + i * (ROW_H + ROW_GAP);
            AABB rowAABB = new AABB(startX, rowY, startX + ROW_W, rowY + ROW_H);
            rowAABBs.add(rowAABB);

            boolean isCapturing = bind == capturing;
            boolean hovered = rowAABB.contains(mouseX, mouseY);

            Vector4f bg = (hovered || isCapturing)
                    ? new Vector4f(0.5f, 0.5f, 0.5f, 1f)
                    : new Vector4f(0.35f, 0.35f, 0.35f, 1f);

            Vector4f border;
            if (isCapturing)     border = new Vector4f(1f, 0.85f, 0.3f, 1f);
            else if (hovered)    border = new Vector4f(1f, 1f, 1f, 1f);
            else                 border = new Vector4f(1f, 1f, 1f, 0.3f);

            new ColoredRectBatch()
                    .rectSized(startX, rowY, ROW_W, ROW_H, RectColors.verticalGradient(bg, bg))
                    .build().tryDraw().close();

            float bt = 2f;
            new ColoredRectBatch()
                    .rectSized(startX, rowY, ROW_W, bt, RectColors.verticalGradient(border, border))
                    .rectSized(startX, rowY + ROW_H - bt, ROW_W, bt, RectColors.verticalGradient(border, border))
                    .rectSized(startX, rowY + bt, bt, ROW_H - bt * 2, RectColors.verticalGradient(border, border))
                    .rectSized(startX + ROW_W - bt, rowY + bt, bt, ROW_H - bt * 2, RectColors.verticalGradient(border, border))
                    .build().tryDraw().close();

            float nameY = rowY + (ROW_H - game.getRenderer().smallFont.getTextHeight()) / 2f;
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(bindLabels.get(i), startX + PADDING, nameY).withShadow(true))
                    .build().tryDraw().close();

            String keyText = isCapturing ? "..." : bind.getDisplayName();
            float keyW = game.getRenderer().smallFont.getTextWidth(keyText);
            float keyX = startX + ROW_W - PADDING - keyW;
            Vector4f keyColor = isCapturing
                    ? new Vector4f(1f, 0.9f, 0.4f, 1f)
                    : new Vector4f(1f, 1f, 1f, 1f);
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(keyText, keyX, nameY).withShadow(true).withColor(keyColor))
                    .build().tryDraw().close();
        }
    }

    @Override
    public void onMousePress(double mouseX, double mouseY, int button) {
        for (Slider s : sliders) s.onMousePress(mouseX, mouseY, button);
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        boolean anySliderHit = sliders.stream().anyMatch(s -> s.contains(mouseX, mouseY));
        boolean wasDragging  = sliders.stream().anyMatch(Slider::isDragging);

        for (Slider s : sliders) s.onMouseRelease(mouseX, mouseY, button);

        if (anySliderHit || wasDragging)
            return;

        for (Button b : buttons) {
            if (b.contains(mouseX, mouseY)) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    capturing = null;
                    b.onMouseRelease(mouseX, mouseY, button);
                }
                return;
            }
        }

        if (capturing != null) {
            for (int i = 0; i < rowAABBs.size(); i++) {
                if (rowAABBs.get(i).contains(mouseX, mouseY)) {
                    if (binds.get(i) != capturing) capturing = binds.get(i);
                    return;
                }
            }

            capturing.setBinding(button, KeyType.Mouse);
            capturing = null;
            return;
        }


        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < rowAABBs.size(); i++) {
                if (rowAABBs.get(i).contains(mouseX, mouseY)) {
                    capturing = binds.get(i);
                    return;
                }
            }
        }
    }

    @Override
    public void onKeyPress(int key) {
        if (capturing == null) return;
        if (key == GLFW.GLFW_KEY_ESCAPE) return;  // отдаём shouldCloseOnEsc
        capturing.setBinding(key, KeyType.Keyboard);
        capturing = null;
    }

    @Override
    public void close() {
        if (capturing != null) {
            capturing = null;
            return;
        }
        game.setScreen(previousScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (capturing != null) {
            capturing = null;
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    private static String prettify(String fieldName) {
        String[] parts = fieldName.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}