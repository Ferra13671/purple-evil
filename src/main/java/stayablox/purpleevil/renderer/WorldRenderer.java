package stayablox.purpleevil.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;
import stayablox.purpleevil.GameInstance;
import stayablox.purpleevil.model.Sprite;
import stayablox.purpleevil.renderer.batch.impl.BasicTextureBatch;
import stayablox.purpleevil.renderer.batch.impl.ColoredRectBatch;
import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.utils.Utils;
import stayablox.purpleevil.world.entity.*;
import stayablox.purpleevil.world.room.Room;
import stayablox.purpleevil.world.block.Block;
import stayablox.purpleevil.world.weapon.SwordWeapon;
import stayablox.purpleevil.world.weapon.Weapon;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.List;

public class WorldRenderer implements GameInstance {
    public static final int MAX_BLOCKS_IN_WIDTH = 30;
    private final Renderer renderer;

    public WorldRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render() {
        setWorldMatrices();
        game.getWindow().getFramebuffer().bind(true);
        renderRooms();
        renderEntities();
    }

    private void renderRooms() {
        TextureAtlas textureAtlas = game.getTextures().worldAtlas;

        for (Room room : game.getWorld().rooms) {
            BasicTextureBatch textureBatch = new BasicTextureBatch().texture(textureAtlas);

            for (Block block : room.blocks) {
                String textureId = block.textureId;

                if (textureId != null && game.getTextures().getWorldTexture(textureId) != null) {
                    textureBatch
                            .rectSized(
                                    block.x,
                                    block.y,
                                    1,
                                    1,
                                    textureAtlas.getBorder(game.getTextures().getWorldTexture(textureId))
                            );
                }
            }

            textureBatch
                    .build()
                    .tryDraw()
                    .close();
        }
    }

    private void renderEntities() {
        for (Room room : game.getWorld().rooms) {
            for (Entity entity : room.entities)
                renderEntity(entity);
        }

        Player player = game.getGameSession().player;
        renderDashTrail(player);
        renderEntity(player);
    }

    private void renderDashTrail(Player player) {
        List<Vector2d> trail = player.getDashTrail();
        if (trail.isEmpty()) return;

        Sprite sprite = player.model.getCurrentSprite();
        if (sprite == null) return;

        for (int i = 0; i < trail.size(); i++) {
            Vector2d pos = trail.get(i);

            float alpha = (float) (i + 1) / trail.size() * 0.4f;

            Vector4f color = player.isShockDashing()
                    ? new Vector4f(0.5f, 0.8f, 1f, alpha)
                    : new Vector4f(1f, 0.8f, 1f, alpha);

            AABB aabb = new AABB(player.modelAABB).move(pos);
            double xLength = aabb.getXLength();

            new BasicTextureBatch(() -> CometRenderer.getShaderColor().setColor(color))
                    .texture(game.getTextures().entityAtlas)
                    .rectSized(
                            (float) (aabb.getMinX() - ((xLength * sprite.width()) - xLength) / 2),
                            (float) aabb.getMinY(),
                            (float) aabb.getXLength() * sprite.width(),
                            (float) aabb.getYLength() * sprite.height(),
                            game.getTextures().entityAtlas.getBorder(
                                    game.getTextures().getEntityTexture(sprite.textureId())
                            )
                    )
                    .build()
                    .tryDraw()
                    .close();
        }
    }

    private void renderEntity(Entity entity) {
        Vector4f color = new Vector4f(1f);

        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.isInvincible() && !livingEntity.isDying()) {
                int blinkPhase = (int) (livingEntity.getInvincibilityTimer() * 10);
                if (blinkPhase % 2 == 0) return;
            }

            if (livingEntity.getPoisonTimer() > 0)
                color.set(0, 0.9f, 0, 1f);
            else if (livingEntity.isShocked())
                color.set(0.4f, 0.7f, 1f, 1f);   // ← синий тинт для шокированных
        }

        if (entity instanceof SlimeEntity se)
            color.mul(se.color);

        AABB aabb = new AABB(entity.modelAABB).move(Utils.lerp(entity.prevPosition, entity.position, game.getTicker().getTickDelta()));
        Sprite sprite = entity.model.getCurrentSprite();

        // ===== Проектили рендерятся с поворотом по yaw =====
        if (entity instanceof DemonArrowEntity) {
            renderRotatedEntity(entity, aabb, sprite, color);
            return;
        }

        double xLength = aabb.getXLength();
        new BasicTextureBatch(() -> CometRenderer.getShaderColor().setColor(color))
                .texture(game.getTextures().entityAtlas)
                .rectSized(
                        (float) (aabb.getMinX() - ((xLength * sprite.width()) - xLength) / 2),
                        (float) aabb.getMinY(),
                        (float) aabb.getXLength() * sprite.width(),
                        (float) aabb.getYLength() * sprite.height(),
                        game.getTextures().entityAtlas.getBorder(
                                game.getTextures().getEntityTexture(sprite.textureId())
                        )
                )
                .build()
                .tryDraw()
                .close();

        if (entity instanceof LivingEntity le && !le.isDying()) {
            renderLivingEntityWeapon(aabb, le);

            if (le instanceof EnemyEntity)
                renderHealthBar(le, aabb);
        }
    }

    private void renderRotatedEntity(Entity entity, AABB aabb, Sprite sprite, Vector4f color) {
        double cx = aabb.getMinX() + aabb.getXLength() / 2;
        double cy = aabb.getMinY() + aabb.getYLength() / 2;

        float renderW = (float) (aabb.getXLength() * sprite.width());
        float renderH = (float) (aabb.getYLength() * sprite.height());
        float yaw = (float) Utils.lerp(entity.prevYaw, entity.yaw, game.getTicker().getTickDelta());

        this.renderer.view.pushMatrix().translate((float) cx, (float) cy, 0);

        new BasicTextureBatch(() -> CometRenderer.getShaderColor().setColor(color))
                .texture(game.getTextures().entityAtlas)
                .rectPositioned(
                        -renderW / 2,
                        -renderH / 2,
                        renderW,
                        renderH,
                        game.getTextures().entityAtlas.getBorder(
                                game.getTextures().getEntityTexture(sprite.textureId())
                        ),
                        new Matrix4f().rotateZ((float) Math.toRadians(yaw))
                )
                .build()
                .tryDraw()
                .close();

        this.renderer.view.popMatrix();
    }

    private void renderHealthBar(LivingEntity entity, AABB aabb) {
        double maxHealth = entity.getAttributeValue(EntityAttributes.MAX_HEALTH);
        double currentHealth = entity.getHealth();

        if (currentHealth <= 0)
            return;

        float progress = (float) (currentHealth / maxHealth);

        // Настройки полоски
        float barWidth = (float) aabb.getXLength() * 0.8f; // Чуть уже самого врага
        float barHeight = 0.12f; // Толщина полоски
        float x = (float) aabb.getMinX() + (float) aabb.getXLength() * 0.1f; // Центрируем
        float y = (float) aabb.getMinY() - 0.25f; // Смещаем вниз под врага

        // 1. Фон (черный, полупрозрачный)
        Vector4f bgColor = new Vector4f(0f, 0f, 0f, 0.7f);
        Vector4f fillColor = new Vector4f(0.8f, 0.1f, 0.1f, 1f);

        new ColoredRectBatch()
                .rectSized(x, y, barWidth, barHeight, RectColors.verticalGradient(bgColor, bgColor))
                .rectSized(x, y, barWidth * progress, barHeight, RectColors.verticalGradient(fillColor, fillColor))
                .build().tryDraw().close();
    }

    private void renderLivingEntityWeapon(AABB aabb, LivingEntity livingEntity) {
        Weapon weapon = livingEntity.getWeapon();
        if (weapon != null) {
            float renderAngle = (float) livingEntity.getRenderYaw();

            // Если это меч — берем угол из меча (там уже учтен взмах)
            if (weapon instanceof SwordWeapon sword) {
                renderAngle = sword.getRenderAngle(livingEntity);
            }

            this.renderer.view.pushMatrix().translate(
                    (float) (aabb.getMinX() + livingEntity.getHandPos().x),
                    (float) (aabb.getMinY() + livingEntity.getHandPos().y),
                    0f
            );

            float halfWeaponSize = weapon.getSize() / 2;

            // Рендерим сам меч
            new BasicTextureBatch()
                    .texture(game.getTextures().weaponAtlas)
                    .rectPositioned(
                            -halfWeaponSize,
                            -halfWeaponSize,
                            halfWeaponSize,
                            halfWeaponSize,
                            game.getTextures().weaponAtlas.getBorder(game.getTextures().getWeaponTexture(weapon.getTextureId())),
                            new Matrix4f()
                                    .rotateZ((float) Math.toRadians(renderAngle - 45))
                                    .translate(0.5f, 0.5f, 0)
                    )
                    .build()
                    .tryDraw()
                    .close();

            this.renderer.view.popMatrix();
        }
    }

    private void setWorldMatrices() {
        float aspect    = (float) GameInstance.game.getWindow().getFramebufferWidth() / GameInstance.game.getWindow().getFramebufferHeight();
        float halfW = MAX_BLOCKS_IN_WIDTH * 0.5f;
        float halfH = halfW / aspect;

        this.renderer.projection.identity().ortho(
                -halfW, halfW,
                -halfH, halfH,
                -100, 100
        );

        Vector2f cameraPos = game.getWorld().camera.getLerpedPosition();
        this.renderer.view.identity().translate(
                -cameraPos.x,
                -cameraPos.y,
                0.0f
        );
    }
}