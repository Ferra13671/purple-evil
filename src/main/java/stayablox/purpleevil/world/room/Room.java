package stayablox.purpleevil.world.room;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.block.Block;
import stayablox.purpleevil.world.block.BlockType;
import stayablox.purpleevil.world.entity.Entity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    public final List<Block> blocks = new CopyOnWriteArrayList<>();
    public final List<Entity> entities = new CopyOnWriteArrayList<>();
    public final List<RoomExit> exits = new ArrayList<>();
    public final int centerX;
    public final int centerY;

    public final RoomType roomType;
    @Getter
    private boolean closed = false;

    private AABB cachedBounds;

    public Room(RoomTemplate template, int x, int y) {
        int halfWidth = template.width() / 2;
        int halfHeight = template.height() / 2;
        for (BlockTemplate bt : template.blocks()) {
            this.blocks.add(new Block(
                    bt.blockType(),
                    bt.x() + x - halfWidth,
                    bt.y() + y + halfHeight
            ));
        }
        for (RoomExit e : template.exits()) {
            this.exits.add(new RoomExit(
                    e.x() + x - halfWidth,
                    e.y() + y + halfHeight,
                    e.width(), e.height(), e.side()
            ));
        }

        AABB bounds = getBounds();
        this.centerX = (int) ((bounds.getMinX() + bounds.getMaxX()) * 0.5);
        this.centerY = (int) ((bounds.getMinY() + bounds.getMaxY()) * 0.5);

        this.roomType = template.roomType();
    }

    public Room(List<Block> blocks, RoomType roomType) {
        this.blocks.addAll(blocks);

        AABB bounds = getBounds();
        this.centerX = (int) ((bounds.getMinX() + bounds.getMaxX()) * 0.5);
        this.centerY = (int) ((bounds.getMinY() + bounds.getMaxY()) * 0.5);

        this.roomType = roomType;
    }

    public AABB getBounds() {
        if (this.cachedBounds == null) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Block b : blocks) {
                minX = Math.min(minX, b.x);
                minY = Math.min(minY, b.y);
                maxX = Math.max(maxX, b.x + 1);
                maxY = Math.max(maxY, b.y + 1);
            }
            this.cachedBounds = new AABB(minX, minY, maxX, maxY);
        }
        return cachedBounds;
    }

    public void closeRoom() {
        this.closed = true;

        for (Block block : this.blocks) {
            if (block.blockType == BlockType.Exit) {
                this.blocks.remove(block);
                this.blocks.add(new Block(
                        BlockType.ClosedExit,
                        block.x,
                        block.y
                ));
            }
        }
    }

    public void openRoom() {
        this.closed = false;

        for (Block block : this.blocks) {
            if (block.blockType == BlockType.ClosedExit) {
                this.blocks.remove(block);
                this.blocks.add(new Block(
                        BlockType.Exit,
                        block.x,
                        block.y
                ));
            }
        }
    }

    public boolean intersects(AABB other) {
        AABB b = getBounds();
        return b.getMaxX() > other.getMinX() && b.getMinX() < other.getMaxX()
                && b.getMaxY() > other.getMinY() && b.getMinY() < other.getMaxY();
    }

    public void tick() {
        for (Entity e : this.entities)
            e.tick();
    }

    public List<Block> getBlocks(BlockType blockType) {
        List<Block> list = new ArrayList<>();

        for (Block block : this.blocks)
            if (block.blockType == blockType)
                list.add(block);

        return list;
    }

    @Override
    public String toString() {
        return String.format("Room[%s, %s, %s]", centerX, centerY, blocks);
    }
}