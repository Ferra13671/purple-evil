package govno.world.entity.input;

import govno.utils.AABB;
import govno.world.entity.*;
import govno.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class DemonBossInput extends Input {

    private enum State {
        IDLE,
        TRANSITION,
        SHOOT,
        CHARGE_WINDUP,
        CHARGE,
        RECOVER,
        SUMMON
    }

    // ===== Тайминги =====
    private static final int TRANSITION_DURATION   = 30;   // 1.5 сек "рёв"
    private static final int SHOOT_DURATION        = 20;   // 1 сек поза после выстрела
    private static final int CHARGE_WINDUP_DURATION= 15;   // 0.75 сек телеграф
    private static final int CHARGE_MAX_TICKS      = 25;   // 1.25 сек рывок
    private static final int SUMMON_DURATION       = 40;   // 2 сек анимация
    private static final int SUMMON_INTERVAL       = 4;    // атак между призывами

    // ===== Урон рывка =====
    private static final double CHARGE_DAMAGE_MULT = 1.5;
    private static final double CHARGE_KNOCKBACK   = 2.5;
    private static final double CHARGE_HIT_RADIUS  = 2.0;

    private final DemonBossEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.IDLE;
    private int currentPhase = 1;
    private int timer = 0;
    private int attackCounter = 0;
    private int attacksSinceSummon = 0;

    private final Vector2d chargeDir = new Vector2d();
    private boolean chargeHit = false;

    public DemonBossInput(DemonBossEntity owner, Room room) {
        this.owner = owner;
        this.roomBounds = room.getBounds();
    }

    @Override
    public void tick() {
        if (game.getWorld() == null || game.getWorld().currentRoom == null) {
            vector.set(0, 0);
            return;
        }

        if (!game.getWorld().currentRoom.entities.contains(owner)) {
            vector.set(0, 0);
            return;
        }

        // ===== Проверка смены фазы =====
        int newPhase = computePhase();
        if (newPhase > currentPhase && state != State.TRANSITION) {
            currentPhase = newPhase;
            startTransition();
            return;
        }

        switch (state) {
            case IDLE -> tickIdle();
            case TRANSITION -> tickTransition();
            case SHOOT -> tickShoot();
            case CHARGE_WINDUP -> tickChargeWindup();
            case CHARGE -> tickCharge();
            case RECOVER -> tickRecover();
            case SUMMON -> tickSummon();
        }
    }

    // ============ ФАЗЫ ============

    private int computePhase() {
        double ratio = owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        if (ratio < 0.33) return 3;
        if (ratio < 0.66) return 2;
        return 1;
    }

    private void startTransition() {
        state = State.TRANSITION;
        timer = TRANSITION_DURATION;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);

        // Кратковременная неуязвимость во время рёва.
        // invincibilityTimer в секундах: ticks * delay_ms / 1000.
        double secs = TRANSITION_DURATION * game.getTicker().getTickDelay() / 1000.0;
        owner.setInvincibilityTimer(Math.max(owner.getInvincibilityTimer(), secs));

        if (owner.model != null) {
            if (owner.model.hasAnimation("roar"))
                owner.model.play("roar", true);
            else if (owner.model.hasAnimation("summon"))
                owner.model.play("summon", true);
        }
    }

    private void tickTransition() {
        vector.set(0, 0);
        if (--timer <= 0) {
            state = State.IDLE;
            timer = 0;
            attackCounter = 0;
            attacksSinceSummon = 0;
        }
    }

    // ============ IDLE / ВЫБОР АТАКИ ============

    private void tickIdle() {
        vector.set(0, 0);
        owner.summonedMinions.removeIf(e -> e.removed);

        if (currentPhase < 3 && !hasAliveMinions() && attacksSinceSummon >= SUMMON_INTERVAL) {
            attacksSinceSummon = 0;
            attackCounter++;
            startSummon();
            return;
        }

        attacksSinceSummon++;
        attackCounter++;
        if (attackCounter % 2 == 1) {
            startShoot();
        } else {
            startChargeWindup();
        }
    }

    private boolean hasAliveMinions() {
        for (Entity e : owner.summonedMinions) {
            if (e.removed) continue;
            if (e instanceof LivingEntity le && le.isDying()) continue;
            return true;
        }
        return false;
    }

    // ============ СТРЕЛЬБА ============

    private void startShoot() {
        state = State.SHOOT;
        timer = SHOOT_DURATION;
        vector.set(0, 0);

        fireOrbSpread();
    }

    private void tickShoot() {
        vector.set(0, 0);
        if (--timer <= 0) {
            state = State.IDLE;
            timer = 0;
        }
    }

    private void fireOrbSpread() {
        int count = switch (currentPhase) { case 1 -> 3; case 2 -> 5; default -> 7; };
        double halfSpreadDeg = switch (currentPhase) { case 1 -> 30.0; case 2 -> 45.0; default -> 60.0; };
        double orbSpeed = 0.4;

        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        if (toPlayer.lengthSquared() < 0.01) return;
        double baseAngle = Math.toDegrees(Math.atan2(toPlayer.y, toPlayer.x));

        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0.5 : (double) i / (count - 1);
            double angleDeg = baseAngle - halfSpreadDeg + t * (2 * halfSpreadDeg);
            double rad = Math.toRadians(angleDeg);
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(orbSpeed);

            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position),
                    vel,
                    owner.getAttributeValue(EnemyAttributes.DAMAGE)
            ));
        }
    }

    // ============ РЫВОК ============

    private void startChargeWindup() {
        state = State.CHARGE_WINDUP;
        timer = CHARGE_WINDUP_DURATION;
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        if (toPlayer.lengthSquared() < 0.01) {
            double a = random.nextDouble() * Math.PI * 2;
            chargeDir.set(Math.cos(a), Math.sin(a));
        } else {
            chargeDir.set(toPlayer).normalize();
        }

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickChargeWindup() {
        vector.set(0, 0);
        if (--timer <= 0) {
            state = State.CHARGE;
            timer = CHARGE_MAX_TICKS;
            chargeHit = false;
            owner.setSpeedMultiplier(getChargeSpeedMult());
            vector.set(chargeDir);
        }
    }

    private float getChargeSpeedMult() {
        return switch (currentPhase) { case 1 -> 4.0f; case 2 -> 5.0f; default -> 6.0f; };
    }

    private void tickCharge() {
        vector.set(chargeDir);
        timer--;

        // Ручной хит-детект (phasing пропускает обычные коллизии)
        if (!chargeHit) {
            double dist = new Vector2d(game.getGameSession().player.position)
                    .sub(owner.position).length();
            if (dist <= CHARGE_HIT_RADIUS) {
                chargeHit = true;
                game.getGameSession().player.damage(
                        owner.getAttributeValue(EnemyAttributes.DAMAGE) * CHARGE_DAMAGE_MULT);
                game.getGameSession().player.applyKnockback(owner.position, CHARGE_KNOCKBACK);
            }
        }

        if (timer <= 0) endCharge();
    }

    private void endCharge() {
        owner.setSpeedMultiplier(1f);
        owner.velocity.set(0, 0);
        vector.set(0, 0);

        clampToRoom(owner.position);
        owner.prevPosition.set(owner.position);
        owner.updateAABB();

        state = State.RECOVER;
        timer = switch (currentPhase) { case 1 -> 20; case 2 -> 15; default -> 10; };
    }

    private void tickRecover() {
        vector.set(0, 0);
        if (--timer <= 0) {
            state = State.IDLE;
            timer = 0;
        }
    }

    // ============ ПРИЗЫВ ============

    private void startSummon() {
        state = State.SUMMON;
        timer = SUMMON_DURATION;
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        if (owner.model != null && owner.model.hasAnimation("summon"))
            owner.model.play("summon", true);
    }

    private void tickSummon() {
        vector.set(0, 0);
        if (--timer == SUMMON_DURATION / 2) performSummon();
        if (timer <= 0) {
            state = State.IDLE;
            timer = 0;
        }
    }

    private void performSummon() {
        int flyingCount   = currentPhase == 1 ? 1 : 2;
        int crossbowCount = currentPhase >= 2 ? 1 : 0;

        for (int i = 0; i < flyingCount; i++) {
            FlyingDemonEntity demon = new FlyingDemonEntity(game.getWorld().currentRoom);
            demon.canDropUpgrades = false;
            demon.position.set(owner.position).add(
                    random.nextDouble() * 3 - 1.5,
                    random.nextDouble() * 3 - 1.5);
            demon.updateAABB();
            game.getWorld().currentRoom.entities.add(demon);
            owner.summonedMinions.add(demon);
        }
        for (int i = 0; i < crossbowCount; i++) {
            CrossbowmanDemonEntity cb = new CrossbowmanDemonEntity(game.getWorld().currentRoom);
            cb.canDropUpgrades = false;
            cb.position.set(owner.position).add(
                    random.nextDouble() * 3 - 1.5,
                    random.nextDouble() * 3 - 1.5);
            cb.updateAABB();
            game.getWorld().currentRoom.entities.add(cb);
            owner.summonedMinions.add(cb);
        }
    }

    // ============ УТИЛИТЫ ============

    private void clampToRoom(Vector2d pos) {
        double padding = 2.0;
        pos.x = Math.clamp(pos.x, roomBounds.getMinX() + padding, roomBounds.getMaxX() - padding);
        pos.y = Math.clamp(pos.y, roomBounds.getMinY() + padding, roomBounds.getMaxY() - padding);
    }
}