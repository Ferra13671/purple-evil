package govno.world.entity;

import govno.Keybinds;
import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.PlayerInput;
import govno.world.weapon.Weapon;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.Random;

public class Player extends LivingEntity {

    private double stepTimer = 0;
    private final Random random = new Random();

    public Player(EntityModel model, Weapon weapon) {
        super(
                model,
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(new HashMap<>(){{
                    put(EntityAttributes.MAX_HEALTH, 100d);
                    put(EntityAttributes.REGENERATION, 0.04);
                    put(EntityAttributes.ACCELERATION, 0.6);
                    put(EntityAttributes.SPEED, 0.25);
                    put(EntityAttributes.INVINCIBILITY_TIME, 1d);
                }}),
                100,
                new Vector2d(0.5, 1),
                weapon
        );

        setInput(new PlayerInput());
    }

    @Override
    public void onDying() {
        super.onDying();
        game.getGameSession().onPlayerDying(this);
        setWeapon(null);
    }

    @Override
    public void onDead() {
        super.onDead();
        game.getGameSession().onPlayerDead(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.dying || this.removed)
            return;

        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double mouseX = game.getMouseListener().position.x;
        double mouseY = game.getMouseListener().position.y;
        double dx = mouseX - centerX;
        double dy = centerY - mouseY;
        this.yaw = Math.toDegrees(Math.atan2(dy, dx));

        if (Keybinds.ATTACK.isPressed()) {
            if (!this.weapon.isUsingWeapon())
                this.weapon.startUse(this);
        } else if (this.weapon.isUsingWeapon()) {
            this.weapon.endUse(this);
        }


        float dt = game.getTicker().getTickDelay() / 1000f;
        double speed = this.velocity.length();

        if (speed > 0.05) {
            this.stepTimer -= dt;

            double newStepTimer = 0.15 / speed;
            this.stepTimer = Math.min(this.stepTimer, newStepTimer);

            if (this.stepTimer <= 0) {
                float pitch = 0.8f + random.nextFloat() * 0.4f;
                game.getSoundManager().playRawSound(game.getSounds().STEP, 0.4f, pitch);

                this.stepTimer = newStepTimer;
            }
        }
    }
}