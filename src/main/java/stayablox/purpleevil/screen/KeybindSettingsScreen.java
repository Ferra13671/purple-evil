package stayablox.purpleevil.screen;

import stayablox.purpleevil.Settings;
import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.utils.KeyBind;
import stayablox.purpleevil.utils.KeyType;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class KeybindSettingsScreen extends Screen {
    private static final float ROW_WIDTH = 500f;
    private static final float ROW_HEIGHT = 44f;
    private static final float ROW_GAP = 6f;
    private static final float PADDING = 20f;

    private float bindsStartY;

    private final Screen previousScreen;
    private final List<KeyBind> binds = new ArrayList<>();
    private final List<String> bindLabels = new ArrayList<>();
    private final List<AABB> rowAABBs = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();

    private KeyBind capturing = null;

    public KeybindSettingsScreen(Screen previousScreen) {
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

        float btnW = 200f;
        float btnH = 50f;
        float bottomY = height - btnH - 30f;
        float centerX = width / 2f;
        buttons.add(new Button("Back", centerX - btnW / 2f, bottomY, btnW, btnH,
                () -> game.setScreen(previousScreen)));

        int count = binds.size();
        float bindsBlockH = count > 0 ? count * ROW_HEIGHT + (count - 1) * ROW_GAP : 0f;

        float startY = (height - bindsBlockH) / 2f - 30f;
        
        this.bindsStartY = startY;
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        renderBackground(mouseX, mouseY);

        String title = "KEYBINDS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 20f).withShadow(true))
                .build().tryDraw().close();

        renderBinds(width, height, mouseX, mouseY);

        for (Button b : buttons) b.render(mouseX, mouseY);

        String hint = capturing != null
                ? "Press any key or click to rebind, ESC to cancel"
                : "Click a keybind to rebind";
        float hintW = game.getRenderer().smallFont.getTextWidth(hint);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(hint, (width - hintW) / 2f, height - 110f).withShadow(true))
                .build().tryDraw().close();
    }

    private void renderBinds(int width, int height, double mouseX, double mouseY) {
        rowAABBs.clear();
        int count = binds.size();
        if (count == 0) return;

        float startY = this.bindsStartY;
        float startX = (width - ROW_WIDTH) / 2f;

        for (int i = 0; i < count; i++) {
            KeyBind bind = binds.get(i);
            float rowY = startY + i * (ROW_HEIGHT + ROW_GAP);
            AABB rowAABB = new AABB(startX, rowY, startX + ROW_WIDTH, rowY + ROW_HEIGHT);
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
                    .rectSized(startX, rowY, ROW_WIDTH, ROW_HEIGHT, RectColors.verticalGradient(bg, bg))
                    .build().tryDraw().close();

            float bt = 2f;
            new ColoredRectBatch()
                    .rectSized(startX, rowY, ROW_WIDTH, bt, RectColors.verticalGradient(border, border))
                    .rectSized(startX, rowY + ROW_HEIGHT - bt, ROW_WIDTH, bt, RectColors.verticalGradient(border, border))
                    .rectSized(startX, rowY + bt, bt, ROW_HEIGHT - bt * 2, RectColors.verticalGradient(border, border))
                    .rectSized(startX + ROW_WIDTH - bt, rowY + bt, bt, ROW_HEIGHT - bt * 2, RectColors.verticalGradient(border, border))
                    .build().tryDraw().close();

            float nameY = rowY + (ROW_HEIGHT - game.getRenderer().smallFont.getTextHeight()) / 2f;
            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(bindLabels.get(i), startX + PADDING, nameY).withShadow(true))
                    .build().tryDraw().close();

            String keyText = isCapturing ? "..." : bind.getDisplayName();
            float keyW = game.getRenderer().smallFont.getTextWidth(keyText);
            float keyX = startX + ROW_WIDTH - PADDING - keyW;
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
        // В этом экране больше нет слайдеров
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
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
        if (key == GLFW.GLFW_KEY_ESCAPE) return;
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