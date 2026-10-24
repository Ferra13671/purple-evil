package govno.world.room;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoomType {
    Spawn("spawn"),
    Exit("exit"),
    Tunnel("tunnel"),
    Combat("combat"),
    Lobby("lobby");

    public final String id;

    public static RoomType fromId(String id) {
        for (RoomType type : values())
            if (type.id.equals(id))
                return type;

        return null;
    }
}
