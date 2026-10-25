package govno.renderer;

import com.ferra13671.cometrenderer.CometRenderer;
import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;
import govno.GameInstance;
import govno.model.Sprite;
import govno.renderer.batch.impl.BasicTextureBatch;
import govno.renderer.batch.impl.ColoredRectBatch;
import govno.utils.AABB;
import govno.utils.Utils;
import govno.world.entity.EnemyEntity;
import govno.world.entity.Entity;
import govno.world.entity.EntityAttributes;
import govno.world.entity.LivingEntity;
import govno.world.room.Room;
import govno.world.block.Block;
import govno.world.weapon.SwordWeapon;
import govno.world.weapon.Weapon;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

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

        renderEntity(game.getGameSession().player);
    }

    private void renderEntity(Entity entity) {
        Vector4f color = new Vector4f(1f);

        if (entity instanceof LivingEntity livingEntity) {
            // Если сущность неуязвима, она мигает (пропускает каждый второй кадр)
            if (livingEntity.isInvincible() && !livingEntity.isDying()) {
                int blinkPhase = (int) (livingEntity.getInvincibilityTimer() * 10);
                if (blinkPhase % 2 == 0) {
                    return; // Пропускаем рендер
                }
            }

            if (livingEntity.getPoisonTimer() > 0)
                color.set(0, 0.9f, 0, 1f);
        }

        AABB aabb = new AABB(entity.modelAABB).move(Utils.lerp(entity.prevPosition, entity.position, game.getTicker().getTickDelta()));
        Sprite sprite = entity.model.getCurrentSprite();

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

        if (entity instanceof LivingEntity le) {
            renderLivingEntityWeapon(aabb, le);

            if (le instanceof EnemyEntity)
                renderHealthBar(le, aabb);
        }
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