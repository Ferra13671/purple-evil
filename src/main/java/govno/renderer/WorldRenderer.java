package govno.renderer;

import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;
import govno.model.Sprite;
import govno.utils.AABB;
import govno.GameInstance;
import govno.utils.Utils;
import govno.renderer.batch.impl.BasicTextureBatch;
import govno.world.entity.LivingEntity;
import govno.world.room.Room;
import govno.world.block.Block;
import govno.world.weapon.Weapon;
import org.joml.Matrix4f;
import org.joml.Vector2f;

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

        renderLivingEntity(game.getWorld().player);
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

    private void renderLivingEntity(LivingEntity livingEntity) {
        AABB aabb = new AABB(livingEntity.modelAABB).move(Utils.lerp(livingEntity.prevPosition, livingEntity.position, game.getTicker().getTickDelta()));
        Sprite sprite = livingEntity.model.getCurrentSprite();

        new BasicTextureBatch()
                .texture(game.getTextures().entityAtlas)
                .rectSized(
                        (float) aabb.getMinX(), (float) aabb.getMinY(), sprite.width(), sprite.height(),
                        game.getTextures().entityAtlas.getBorder(
                                game.getTextures().getEntityTexture(sprite.textureId())
                        )
                )
                .build()
                .tryDraw()
                .close();

        Weapon weapon = livingEntity.getWeapon();
        if (weapon != null) {
            this.renderer.view.pushMatrix().translate(
                    (float) (aabb.getMinX() + livingEntity.getHandPos().x),
                    (float) (aabb.getMinY() + livingEntity.getHandPos().y),
                    0f
            );

            float halfWeaponSize = weapon.getSize() / 2;

            new BasicTextureBatch()
                    .texture(game.getTextures().weaponAtlas)
                    .rectPositioned(
                            -halfWeaponSize,
                            -halfWeaponSize,
                            halfWeaponSize,
                            halfWeaponSize,
                            game.getTextures().weaponAtlas.getBorder(game.getTextures().getWeaponTexture(weapon.getTextureId())),
                            new Matrix4f()
                                    .rotateZ((float) Math.toRadians(livingEntity.getRenderYaw() - 45))
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
