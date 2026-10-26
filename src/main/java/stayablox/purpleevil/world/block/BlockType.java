package stayablox.purpleevil.world.block;

import com.ferra13671.cometrenderer.utils.Pair;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BlockType {
    Wall(true, 'W', new Pair[]{
            new Pair<>(0.7f, "wall"),
            new Pair<>(0.15f, "wall-cracked"),
            new Pair<>(0.15f, "wall-mossy")
    }),
    WallWithTorch(true, 'T', new Pair[]{
            new Pair<>(1f, "wall-torch")
    }),
    Floor(false, '.', new Pair[]{
            new Pair<>(0.97f, "floor"),
            new Pair<>(0.02f, "floor-rubble"),
            new Pair<>(0.01f, "floor-bones")
    }),
    PortalFloor(false, 'P', new Pair[]{
            new Pair<>(1f, "floor-runes")
    }),
    Void(false, ' ', null),
    Exit(false, '@', new Pair[]{
            new Pair<>(1f, "wall-wood-dark")
    }),
    ClosedExit(true, 'C', new Pair[]{
            new Pair<>(1f, "wall-wood")
    }),
    SealedExit(true, '#', new Pair[]{
            new Pair<>(1f, "rock")
    });

    public final boolean hasCollision;
    public final Character id;
    public final Pair<Float, String>[] textureVariants;

    public static BlockType fromId(Character id) {
        for (BlockType type : values())
            if (type.id.equals(id))
                return type;

        return null;
    }
}
