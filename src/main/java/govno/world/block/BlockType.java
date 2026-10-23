package govno.world.block;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum BlockType {
    Wall('W'),
    Floor('.'),
    Void(' '),
    Exit('@');

    public final Character id;

    public static BlockType fromId(Character id) {
        for (BlockType type : values())
            if (type.id.equals(id))
                return type;

        return null;
    }
}
