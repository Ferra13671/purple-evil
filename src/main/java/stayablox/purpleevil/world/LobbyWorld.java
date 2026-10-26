package stayablox.purpleevil.world;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.block.Block;
import stayablox.purpleevil.world.block.BlockType;
import stayablox.purpleevil.world.entity.PortalEntity;
import stayablox.purpleevil.world.entity.ShopEntity;
import stayablox.purpleevil.world.room.Room;
import stayablox.purpleevil.world.room.RoomTemplate;
import org.joml.Vector2d;

import java.util.List;

public class LobbyWorld extends World {

    public LobbyWorld() {
        super(createLobbyRooms());

        spawnLobbyPortal();
        spawnShop();
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

    private void spawnShop() {
        Room room = this.rooms.getFirst();
        AABB bounds = room.getBounds();

        double shopX = (bounds.getMinX() + bounds.getMaxX()) / 2.0;
        double shopY = bounds.getMaxY() - 2.0;

        ShopEntity shop = new ShopEntity(new Vector2d(shopX, shopY));
        room.entities.add(shop);
    }

    public static Block findPortalBlock(List<Block> blocks) {
        for (Block b : blocks)
            if (b.blockType == BlockType.PortalFloor)
                return b;

        return null;
    }
}