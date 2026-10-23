package govno.renderer;

import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;
import govno.AABB;
import govno.GameInstance;
import govno.Utils;
import govno.renderer.batch.impl.BasicRectBatch;
import govno.renderer.batch.impl.BasicTextureBatch;
import govno.world.room.Room;
import govno.world.World;
import govno.world.block.Block;
import govno.world.entity.Player;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class WorldRenderer implements GameInstance {
    public static final int MAX_BLOCKS_IN_WIDTH = 30;
    private final Renderer renderer;

    public WorldRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render() {
        World world = game.getWorld();
        Room room = world.currentRoom;

        if (room != null) {
            setWorldMatrices();

            game.getWindow().getFramebuffer().bind(true);

            renderRoom(room);

            Player player = game.getWorld().player;
            AABB aabb = new AABB(player.modelAABB).move(Utils.lerp(player.prevPosition, player.position, game.getTicker().getTickDelta()));
            new BasicRectBatch()
                    .rectPositioned((float) aabb.getMinX(), (float) aabb.getMinY(), (float) aabb.getMaxX(), (float) aabb.getMaxY())
                    .build()
                    .tryDraw()
                    .close();
        }
    }

    private void renderRoom(Room room) {
        TextureAtlas textureAtlas = game.getTextures().worldAtlas;
        BasicTextureBatch textureBatch = new BasicTextureBatch().texture(textureAtlas);

        for (Block block : room.blocks)
            for (String id : block.textureIds)
                textureBatch
                        .rectSized(
                            block.x,
                            block.y,
                            1,
                            1,
                                textureAtlas.getBorder(game.getTextures().getWorldTexture(id))
                        );

        textureBatch
                .build()
                .tryDraw()
                .close();
    }

    private void setWorldMatrices() {
        float aspect    = (float) GameInstance.game.getWindow().getFramebufferWidth() / GameInstance.game.getWindow().getFramebufferHeight();
        float halfW = MAX_BLOCKS_IN_WIDTH * 0.5f;
        float halfH = halfW / aspect;

        this.renderer.projection = new Matrix4f().ortho(
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
