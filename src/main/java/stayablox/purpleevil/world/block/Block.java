package stayablox.purpleevil.world.block;

import com.ferra13671.cometrenderer.utils.Pair;
import stayablox.purpleevil.utils.AABB;

import java.util.concurrent.ThreadLocalRandom;

public class Block {
    public final BlockType blockType;
    public final int x;
    public final int y;
    public final AABB collisionBox;
    public final String textureId;

    public Block(BlockType blockType, int x, int y) {
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.collisionBox = blockType.hasCollision ? new AABB(x, y, x + 1, y + 1) : null;

        if (blockType.textureVariants != null) {
            float random = ThreadLocalRandom.current().nextFloat();
            float f = 0;
            for (Pair<Float, String> textureVariants : blockType.textureVariants) {
                if (f <= random && random <= f + textureVariants.left()) {
                    this.textureId = textureVariants.right();
                    return;
                }
                f += textureVariants.left();
            }

        }
        this.textureId = null;
    }

    @Override
    public String toString() {
        return String.format("Block[%s;%s]", x, y);
    }
}
