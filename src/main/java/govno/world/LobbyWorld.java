package govno.world;

import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.entity.PortalEntity;
import govno.world.room.Room;
import govno.world.room.RoomTemplate;
import org.joml.Vector2d;

import java.util.List;

public class LobbyWorld extends World {

    public LobbyWorld() {
        super(createLobbyRooms());

        spawnLobbyPortal();
    }

    private static List<Room> createLobbyRooms() {
        RoomTemplate lobbyTemplate = game.getRoomTemplates().templates.get("lobby");
        
        Room lobbyRoom = new Room(lobbyTemplate, 0, 0);
        return List.of(lobbyRoom);
    }

    private void spawnLobbyPortal() {
        Room room = this.rooms.getFirst();
        Block portalBlock = findPortalBlock(room.blocks);

        if (portalBlock != null) {
            PortalEntity portal = new PortalEntity(
                    new Vector2d(
                            portalBlock.x + 0.5,
                            portalBlock.y + 0.5
                    ),
                    () -> game.getGameSession().startDungeon()
            );
            room.entities.add(portal);
        }
    }

    public static Block findPortalBlock(List<Block> blocks) {
        for (Block b : blocks)
            if (b.blockType == BlockType.PortalFloor)
                return b;

        return null;
    }
}