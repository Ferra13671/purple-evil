package govno.world;

import govno.GameInstance;
import govno.Utils;
import org.joml.Vector2f;

public class Camera implements GameInstance {
    public final Vector2f position = new Vector2f();
    public final Vector2f prevPosition = new Vector2f();

    public void tick() {
        this.prevPosition.set(this.position);
        this.position.set(game.getWorld().player.prevPosition);
    }

    public Vector2f getLerpedPosition() {
        return Utils.lerp(this.prevPosition, this.position, game.getTicker().getTickDelta());
    }
}
