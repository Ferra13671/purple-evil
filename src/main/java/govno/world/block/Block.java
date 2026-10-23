package govno.world.block;

import govno.AABB;

public class Block {
    public final BlockType blockType;
    public final int x;
    public final int y;
    public final AABB collisionBox;
    public final String[] textureIds;

    public Block(BlockType blockType, int x, int y, String[] textureIds) {
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.collisionBox = blockType == BlockType.Wall ? new AABB(x, y, x + 1, y + 1) : null;
        this.textureIds = textureIds;
    }
}
