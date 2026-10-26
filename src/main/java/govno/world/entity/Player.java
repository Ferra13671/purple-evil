package govno.world.entity;

import govno.Settings;
import govno.model.EntityModel;
import govno.utils.AABB;
import govno.world.attribute.Attributes;
import govno.world.entity.input.PlayerInput;
import govno.world.weapon.SwordWeapon;
import govno.world.weapon.Weapon;
import lombok.Getter;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Player extends LivingEntity {

    private static final double SHOCK_DURATION_SECONDS = 10.0;

    private double stepTimer = 0;
    private final Random random = new Random();

    @Getter
    private int dashCooldown = 0;
    @Getter
    private int ticksSinceDash = 1000;
    @Getter
    private final List<Vector2d> dashTrail = new ArrayList<>();
    private static final int DASH_DURATION = 20;

    @Getter
    private int shockDashCooldown = 0;
    @Getter
    private boolean shockDashing = false;
    private int shockDashTicks = 0;
    private final Set<Entity> shockedThisDash = new HashSet<>();

    public Player(EntityModel model, Weapon weapon) {
        super(
                model,
                new AABB(-0.5,  -0.5, 0.5, 0.5),
                new Attributes(game.getAttributesTemplates().templates.get("player")),
                new Vector2d(0.5, 1),
                weapon
        );

        setInput(new PlayerInput());
    }

    /** Маг — тот, у кого меч (см. GameSession: knight=staff, mage=sword). */
    public boolean isMage() {
        return this.weapon instanceof SwordWeapon;
    }

    @Override
    public void onDying() {
        super.onDying();
        game.getGameSession().onPlayerDying(this);
    }

    @Override
    public void onDead() {
        super.onDead();
        game.getGameSession().onPlayerDead(this);
    }

    @Override
    public void damage(double damage) {
        if (isInvincible() || this.dying)
            return;

        game.getSoundManager().playSound(game.getSounds().PLAYER_DAMAGE, 1f, 0.8f + random.nextFloat() * 0.6f);

        super.damage(damage);
    }

    public float getDashCooldownProgress() {
        if (this.dashCooldown <= 0) return 0f;
        double max = getAttributeValue(PlayerAttributes.DASH_DELAY);
        if (max <= 0) return 0f;
        return (float) Math.clamp(this.dashCooldown / max, 0.0, 1.0);
    }

    public float getShockDashCooldownProgress() {
        if (this.shockDashCooldown <= 0) return 0f;
        double max = getAttributeValue(PlayerAttributes.SHOCK_DASH_DELAY);
        if (max <= 0) return 0f;
        return (float) Math.clamp(this.shockDashCooldown / max, 0.0, 1.0);
    }

    public boolean canUseShockDash() {
        return isMage()
                && this.shockDashCooldown <= 0
                && this.dashCooldown <= 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.dying || this.removed)
            return;

        if (this.dashCooldown > 0)
            this.dashCooldown--;
        if (this.shockDashCooldown > 0)
            this.shockDashCooldown--;
        this.ticksSinceDash++;

        // Завершение окна shock dash
        if (this.shockDashTicks > 0) {
            this.shockDashTicks--;
            if (this.shockDashTicks <= 0) {
                this.shockDashing = false;
                this.shockedThisDash.clear();
            }
        }

        // Триггеры деша (блокируются шоком)
        if (!this.isShocked()) {
            if (Settings.DASH.isPressed() && this.dashCooldown <= 0) {
                startDash();
                Settings.DASH.setPressed(false);
            }

            if (Settings.SHOCK_DASH.isPressed() && canUseShockDash()) {
                startShockDash();
                Settings.SHOCK_DASH.setPressed(false);
            }
        }

        if (this.ticksSinceDash < DASH_DURATION) {
            this.dashTrail.add(new Vector2d(this.position));
            if (this.dashTrail.size() > 6)
                this.dashTrail.removeFirst();
        } else if (!this.dashTrail.isEmpty())
            this.dashTrail.removeFirst();

        int width = game.getWindow().getFramebufferWidth();
        int height = game.getWindow().getFramebufferHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double mouseX = game.getMouseListener().position.x;
        double mouseY = game.getMouseListener().position.y;
        double dx = mouseX - centerX;
        double dy = centerY - mouseY;
        this.yaw = Math.toDegrees(Math.atan2(dy, dx));

        if (this.weapon != null) {
            if (Settings.ATTACK.isPressed()) {
                if (!this.weapon.isUsingWeapon())
                    this.weapon.startUse(this);
            } else if (this.weapon.isUsingWeapon()) {
                this.weapon.endUse(this);
            }
        }

        float dt = game.getTicker().getTickDelay() / 1000f;
        double speed = this.velocity.length();

        if (speed > 0.05) {
            this.stepTimer -= dt;

            double newStepTimer = 0.15 / speed;
            this.stepTimer = Math.min(this.stepTimer, newStepTimer);

            if (this.stepTimer <= 0) {
                float pitch = 0.8f + random.nextFloat() * 0.4f;
                game.getSoundManager().playSound(game.getSounds().STEP, 0.4f, pitch);

                this.stepTimer = newStepTimer;
            }
        }
    }

    @Override
    public void onEntityCollision(Entity other, Vector2dc out) {
        super.onEntityCollision(other, out);

        if (!this.shockDashing)
            return;
        if (!(other instanceof EnemyEntity enemy))
            return;
        if (this.shockedThisDash.contains(enemy))
            return;

        enemy.applyShock(SHOCK_DURATION_SECONDS);
        this.shockedThisDash.add(enemy);
    }

    private void startDash() {
        int dashDelay = (int) getAttributeValue(PlayerAttributes.DASH_DELAY);

        this.dashCooldown = dashDelay;

        this.shockDashCooldown += dashDelay;

        this.ticksSinceDash = 0;

        Vector2d dir = new Vector2d(this.input.vector);
        if (dir.lengthSquared() < 0.1)
            dir.set(this.facing.dx(), this.facing.dy());
        dir.normalize();

        double dashSpeed = getAttributeValue(PlayerAttributes.DASH_SPEED);
        this.velocity.set(dir.mul(dashSpeed));
    }

    private void startShockDash() {
        this.dashCooldown = (int) getAttributeValue(PlayerAttributes.DASH_DELAY);
        this.shockDashCooldown = (int) getAttributeValue(PlayerAttributes.SHOCK_DASH_DELAY);
        this.ticksSinceDash = 0;
        this.shockDashing = true;
        this.shockDashTicks = DASH_DURATION;
        this.shockedThisDash.clear();
        this.invincibilityTimer = getAttributeValue(EntityAttributes.INVINCIBILITY_TIME);

        Vector2d dir = new Vector2d(this.input.vector);
        if (dir.lengthSquared() < 0.1)
            dir.set(this.facing.dx(), this.facing.dy());
        dir.normalize();

        double dashSpeed = getAttributeValue(PlayerAttributes.DASH_SPEED) * 2.5;
        this.velocity.set(dir.mul(dashSpeed));
    }
}