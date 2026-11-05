package stayablox.purpleevil.screen;

import stayablox.purpleevil.renderer.RectColors;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.renderer.batch.impl.text.RenderText;
import stayablox.purpleevil.renderer.batch.impl.text.TextBatch;
import stayablox.purpleevil.utils.AABB;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class CreditsScreen extends Screen {
    private final Screen previousScreen;
    private final List<Button> buttons = new ArrayList<>();
    private final List<MemberCard> members = new ArrayList<>();

    private static final String TEAM_NAME = "СТАЯ БЛОХ";
    private static final String SUBTITLE = "PRESENTS";

    public CreditsScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;

        members.add(new MemberCard("Ferra13671", "https://github.com/Ferra13671", "Lead Programmer • Engine Architect"));
        members.add(new MemberCard("Jaroshar", "https://github.com/Jaroshar", "Art Director • Pixel Artist"));
        members.add(new MemberCard("Mandalor12729", "https://github.com/Mandalor12729", "Concept Artist • Visual Designer"));
        members.add(new MemberCard("wysco", "https://github.com/wysco", "Audio Sorcerer • Texture Artist"));
    }

    @Override
    public void onOpen(int width, int height) {
        resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        buttons.clear();
        float btnW = 200f;
        float btnH = 50f;

        buttons.add(new Button("Back", (width - btnW) / 2f, height - btnH - 30f,
                btnW, btnH, () -> game.setScreen(previousScreen)));

        float cardW = 750f;
        float cardH = 90f;
        float gap = 15f;

        float startY = height / 2f - 80f;
        float startX = (width - cardW) / 2f;

        for (int i = 0; i < members.size(); i++) {
            MemberCard m = members.get(i);
            m.aabb = new AABB(startX, startY + i * (cardH + gap), startX + cardW, startY + (i + 1) * (cardH + gap) - gap);
            m.x = startX;
            m.y = startY + i * (cardH + gap);
            m.width = cardW;
            m.height = cardH;
        }
    }

    @Override
    public void render(double mouseX, double mouseY) {
        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();

        renderBackground(mouseX, mouseY);

        String title = "CREDITS";
        float titleW = game.getRenderer().bigFont.getTextWidth(title);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(title, (width - titleW) / 2f, 30f)
                        .withShadow(true)
                        .withColor(new Vector4f(1f, 1f, 1f, 1f)))
                .build().tryDraw().close();

        float subW = game.getRenderer().smallFont.getTextWidth(SUBTITLE);
        new TextBatch(game.getRenderer().smallFont)
                .text(new RenderText(SUBTITLE, (width - subW) / 2f, 80f)
                        .withShadow(true)
                        .withColor(new Vector4f(0.6f, 0.6f, 0.6f, 1f)))
                .build().tryDraw().close();

        float teamW = game.getRenderer().bigFont.getTextWidth(TEAM_NAME);
        new TextBatch(game.getRenderer().bigFont)
                .text(new RenderText(TEAM_NAME, (width - teamW) / 2f, 105f)
                        .withShadow(true)
                        .withColor(new Vector4f(0.8f, 0.4f, 1f, 1f))) // Фиолетовый акцент
                .build().tryDraw().close();

        float dividerPos = 0.35f;
        for (MemberCard m : members) {
            boolean hovered = m.aabb != null && m.aabb.contains(mouseX, mouseY);

            Vector4f bg = hovered
                    ? new Vector4f(0.15f, 0.1f, 0.2f, 0.9f)
                    : new Vector4f(0.08f, 0.08f, 0.08f, 0.8f);
            new ColoredRectBatch()
                    .rectSized(m.x, m.y, m.width, m.height, RectColors.oneColor(bg))
                    .build().tryDraw().close();

            float bt = 2f;
            Vector4f borderColor = hovered
                    ? new Vector4f(0.8f, 0.4f, 1f, 1f)
                    : new Vector4f(0.3f, 0.3f, 0.3f, 0.5f);
            new ColoredRectBatch()
                    .rectSized(m.x, m.y, m.width, bt, RectColors.oneColor(borderColor))
                    .rectSized(m.x, m.y + m.height - bt, m.width, bt, RectColors.oneColor(borderColor))
                    .rectSized(m.x, m.y + bt, bt, m.height - bt * 2, RectColors.oneColor(borderColor))
                    .rectSized(m.x + m.width - bt, m.y + bt, bt, m.height - bt * 2, RectColors.oneColor(borderColor))
                    .rectSized(m.x + m.width * dividerPos, m.y + 15f, 1f, m.height - 30f, RectColors.oneColor(new Vector4f(0.4f, 0.4f, 0.4f, 0.4f)))
                    .build().tryDraw().close();

            new TextBatch(game.getRenderer().font)
                    .text(new RenderText(m.name, m.x + 20f, m.y + 15f).withShadow(true))
                    .build().tryDraw().close();

            new TextBatch(game.getRenderer().tinyFont)
                    .text(new RenderText(m.url.replace("https://", ""), m.x + 20f, m.y + 55f)
                            .withShadow(true)
                            .withColor(new Vector4f(0.4f, 0.7f, 1f, 1f)))
                    .build().tryDraw().close();

            Vector4f roleColor = hovered ? new Vector4f(1f, 0.9f, 1f, 1f) : new Vector4f(0.7f, 0.7f, 0.7f, 1f);
            float roleH = game.getRenderer().smallFont.getTextHeight();
            float roleW = game.getRenderer().smallFont.getTextWidth(m.role);

            float rightColStartX = m.x + m.width * dividerPos;
            float rightColWidth = m.width * (1f - dividerPos);
            float roleX = rightColStartX + (rightColWidth - roleW) / 2f;

            new TextBatch(game.getRenderer().smallFont)
                    .text(new RenderText(m.role,
                            roleX,
                            m.y + (m.height - roleH) / 2f)
                            .withShadow(true)
                            .withColor(roleColor))
                    .build().tryDraw().close();
        }

        String hint = "Click on a card to open GitHub profile";
        float hintW = game.getRenderer().tinyFont.getTextWidth(hint);
        new TextBatch(game.getRenderer().tinyFont)
                .text(new RenderText(hint, (width - hintW) / 2f, height - 100f)
                        .withShadow(true)
                        .withColor(new Vector4f(0.5f, 0.5f, 0.5f, 1f)))
                .build().tryDraw().close();

        for (Button b : buttons) {
            b.render(mouseX, mouseY);
        }
    }

    @Override
    public void onMouseRelease(double mouseX, double mouseY, int button) {
        for (Button b : buttons) {
            b.onMouseRelease(mouseX, mouseY, button);
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (MemberCard m : members) {
                if (m.aabb != null && m.aabb.contains(mouseX, mouseY)) {
                    try {
                        Desktop.getDesktop().browse(new URI(m.url));
                    } catch (Exception e) {
                        System.err.println("Cannot open link: " + m.url);
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void close() {
        game.setScreen(previousScreen);
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    private static class MemberCard {
        String name;
        String url;
        String role;

        float x, y, width, height;
        AABB aabb;

        MemberCard(String name, String url, String role) {
            this.name = name;
            this.url = url;
            this.role = role;
        }
    }
}