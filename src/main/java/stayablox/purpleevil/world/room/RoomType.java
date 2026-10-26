package stayablox.purpleevil.world.room;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoomType {
    Spawn(false, "spawn"),
    Exit(false, "exit"),
    Tunnel(false, "tunnel"),
    Combat(true, "combat"),
    Boss(true, "boss"),
    Lobby(false, "lobby");

    public boolean combat;
    public final String id;

    public static RoomType fromId(String id) {
        for (RoomType type : values())
            if (type.id.equals(id))
                return type;

        return null;
    }
}