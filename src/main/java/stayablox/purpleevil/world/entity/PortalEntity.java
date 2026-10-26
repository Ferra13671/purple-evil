package stayablox.purpleevil.world.entity;

import stayablox.purpleevil.model.EntityModel;
import stayablox.purpleevil.utils.AABB;
import org.joml.Vector2dc;

public class PortalEntity extends Entity {
    private final Runnable onEnter;
    private boolean triggered = false;
    private boolean runnableRunned = false;

    public PortalEntity(Vector2dc position, Runnable onEnter) {
        super(new EntityModel(game.getEntityModelTemplates().templates.get("portal")), new AABB(-1, -1, 1, 1));

        this.position.set(position);
        this.prevPosition.set(position);

        this.onEnter = onEnter;
    }

    @Override
    public void tick() {
        if (this.runnableRunned)
            return;

        if (this.triggered) {
            this.onEnter.run();
            this.runnableRunned = true;
        }
    }

    @Override
    public void onWorldCollision(Vector2dc out) {

    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        if (other == game.getGameSession().player)
            this.triggered = true;
    }
}
