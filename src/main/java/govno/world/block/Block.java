package govno.world.block;

import govno.utils.AABB;

public class Block {
    public final BlockType blockType;
    public final int x;
    public final int y;
    public final AABB collisionBox;

    public Block(BlockType blockType, int x, int y) {
        this.blockType = blockType;
        this.x = x;
        this.y = y;
        this.collisionBox = blockType.hasCollidion ? new AABB(x, y, x + 1, y + 1) : null;
    }

    @Override
    public String toString() {
        return String.format("Block[%s;%s]", x, y);
    }
}
