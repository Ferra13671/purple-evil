package govno.model;

import govno.utils.Direction;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class EntityModel {
    @Getter
    private final String name;
    private final Map<Direction, Map<String, Animation>> animations;

    @Setter
    @NonNull
    private Direction direction = Direction.DOWN;

    private String currentAnimation;
    private int frameIndex;
    private float frameTimer;
    @Getter
    private boolean finished;

    public EntityModel(EntityModelTemplate template) {
        this.name = template.name();
        this.animations = new HashMap<>(template.animations());

        stop();
    }

    public void play(String name) {
        play(name, false);
    }

    public void play(String name, boolean forceRestart) {
        if (!forceRestart && name.equals(this.currentAnimation))
            return;


        Animation anim = this.animations.get(this.direction).get(name);
        if (anim == null)
            throw new IllegalArgumentException("Animation not found: " + name);

        this.currentAnimation = name;
        this.frameIndex = 0;
        this.frameTimer = 0f;
        this.finished = false;
    }

    /** Останавливает любую анимацию. Спрайт больше не рисуется. */
    public void stop() {
        play("stay");
    }

    public boolean hasAnimation(String name) {
        return this.animations.get(this.direction).containsKey(name);
    }

    public void tick(float dt) {
        Animation anim = getCurrentAnimation();
        if (anim == null || this.finished) {
            play("stay");
            return;
        }

        this.frameTimer += dt;

        while (this.frameTimer >= anim.frames().get(this.frameIndex).duration()) {
            this.frameTimer -= anim.frames().get(this.frameIndex).duration();
            this.frameIndex++;

            if (this.frameIndex >= anim.frames().size()) {
                if (anim.loop()) {
                    this.frameIndex = 0;
                } else {
                    this.frameIndex = anim.frames().size() - 1;
                    this.finished = true;
                    break;
                }
            }
        }
    }

    public Sprite getCurrentSprite() {
        Animation anim = getCurrentAnimation();

        return anim.frames().get(this.frameIndex).sprite();
    }

    public String getCurrentAnimationName() {
        return this.currentAnimation;
    }

    private Animation getCurrentAnimation() {
        return this.animations.get(this.direction).get(this.currentAnimation);
    }
}