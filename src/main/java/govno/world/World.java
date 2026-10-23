package govno.world;

import govno.GameInstance;
import govno.world.entity.Player;
import govno.world.room.Room;

public class World implements GameInstance {
    public final Camera camera = new Camera();
    public final Player player = new Player();
    public Room currentRoom = new Room(game.getRoomTemplates().templates.get("room-1"), 0, 0);

    public World() {
        this.currentRoom.entities.add(this.player);
    }

    public void tick() {
        if (this.currentRoom != null)
            this.currentRoom.tick();

        this.camera.tick();
    }
}
