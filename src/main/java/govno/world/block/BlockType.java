package govno.world.block;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BlockType {
    Wall(true, 'W', "wall"),
    Floor(false, '.', "floor"),
    Void(false, ' ', null),
    Exit(false, '@', "floor"),
    SealedExit(true, '#', "rock");

    public boolean hasCollidion;
    public final Character id;
    public final String textureId;

    public static BlockType fromId(Character id) {
        for (BlockType type : values())
            if (type.id.equals(id))
                return type;

        return null;
    }
}
