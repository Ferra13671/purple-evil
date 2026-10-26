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
        SUMMON,
        COMPLEX_BARRAGE,  // фаза 5 — сложная карусель (<25%)
        SIMPLE_BARRAGE    // фаза 4 — 8 орбов в 8 направлений одним залпом (25–50%)
    }

    // ===== Базовые тайминги =====
    private static final int TRANSITION_DURATION   = 30;
    private static final int SHOOT_DURATION        = 20;
    private static final int CHARGE_WINDUP_DURATION= 15;
    private static final int CHARGE_MAX_TICKS      = 25;
    private static final int SUMMON_DURATION       = 40;
    private static final int SUMMON_INTERVAL       = 4;

    // ===== Урон рывка =====
    private static final double CHARGE_DAMAGE_MULT = 1.5;
    private static final double CHARGE_KNOCKBACK   = 2.5;
    private static final double CHARGE_HIT_RADIUS  = 2.0;

    // ===== Общее для обеих barrage-фаз =====
    private static final int    BARRAGE_SHOT_COUNT      = 8;      // 8 направлений, 45° между
    private static final double BARRAGE_ORB_SPEED       = 0.35;
    private static final double BARRAGE_ORB_DAMAGE_MULT = 0.5;
    private static final int    BARRAGE_DURATION_TICKS  = 200;    // 10 сек — потом босс умирает

    // ===== Сложная карусель (фаза 5, <25%) =====
    private static final double COMPLEX_BARRAGE_HP_THRESHOLD = 0.25;
    private static final int    COMPLEX_BARRAGE_INTERVAL_BASE = 4;      // было 3
    private static final int    COMPLEX_BARRAGE_INTERVAL_MIN  = 2;      // было 1
    private static final double COMPLEX_BARRAGE_ROTATION_BASE = 2.0;    // было 3.0
    private static final double COMPLEX_BARRAGE_ROTATION_MAX  = 4.0;    // было 6.0
    private static final int    COMPLEX_BARRAGE_FLIP_MIN_INTERVAL = 8;  // было 5
    private static final int    COMPLEX_BARRAGE_FLIP_MAX_INTERVAL = 20; // было 12
    private static final float  COMPLEX_BARRAGE_FLIP_CHANCE     = 0.55f;// было 0.75

    // ===== Простая (фаза 4, 25–50%) =====
    private static final double SIMPLE_BARRAGE_HP_THRESHOLD     = 0.50;
    private static final int    SIMPLE_BARRAGE_VOLLEY_INTERVAL  = 10;  // 500 мс между залпами

    private final DemonBossEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.IDLE;
    private int currentPhase = 1;
    private int timer = 0;
    private int attackCounter = 0;
    private int attacksSinceSummon = 0;

    private int currentSummonDuration = SUMMON_DURATION;

    private final Vector2d chargeDir = new Vector2d();
    private boolean chargeHit = false;

    // ===== Состояние сложной карусели (фаза 5) =====
    private double barrageAngle = 0;
    private int barrageShotTimer = 0;
    private int barrageDirection = 1;
    private int barrageFlipIn = 0;
    private int barrageTicksElapsed = 0;

    // ===== Состояние простой (фаза 4) =====
    private int simpleBarrageShotTimer = 0;
    private int simpleBarrageTicksElapsed = 0;

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
            case COMPLEX_BARRAGE -> tickComplexBarrage();
            case SIMPLE_BARRAGE -> tickSimpleBarrage();
        }
    }

    // ============ АГРЕССИЯ ============

    private double aggression() {
        double maxHp = owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        if (maxHp <= 0) return 0.0;
        double hpRatio = Math.clamp(owner.getHealth() / maxHp, 0.0, 1.0);
        return 1.0 - hpRatio;
    }

    private int scaledShootDuration() {
        return Math.max(12, (int) (SHOOT_DURATION - 8 * aggression()));
    }

    private int scaledChargeWindup() {
        return Math.max(6, (int) (CHARGE_WINDUP_DURATION - 9 * aggression()));
    }

    private int scaledChargeMaxTicks() {
        return (int) (CHARGE_MAX_TICKS + 10 * aggression());
    }

    private int scaledRecover() {
        int phaseBase = switch (currentPhase) { case 1 -> 20; case 2 -> 15; default -> 10; };
        return Math.max(5, (int) (phaseBase - 8 * aggression()));
    }

    private float scaledChargeSpeedMult() {
        float phaseBase = switch (currentPhase) { case 1 -> 4.0f; case 2 -> 5.0f; default -> 6.0f; };
        return phaseBase + 2.0f * (float) aggression();
    }

    private int scaledSummonInterval() {
        return Math.max(2, (int) Math.round(SUMMON_INTERVAL - 2 * aggression()));
    }

    private int scaledSummonDuration() {
        return Math.max(24, (int) (SUMMON_DURATION - 16 * aggression()));
    }

    // ============ ФАЗЫ ============

    private int computePhase() {
        double ratio = owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        if (ratio < COMPLEX_BARRAGE_HP_THRESHOLD) return 5;   // <25% → сложная карусель
        if (ratio < SIMPLE_BARRAGE_HP_THRESHOLD) return 4;    // <50% → простая
        if (ratio < 0.66) return 2;
        return 1;
    }

    private void startTransition() {
        state = State.TRANSITION;
        timer = TRANSITION_DURATION;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);

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
            if (currentPhase >= 5) {
                startComplexBarrage();
            } else if (currentPhase >= 4) {
                startSimpleBarrage();
            } else {
                state = State.IDLE;
                timer = 0;
                attackCounter = 0;
                attacksSinceSummon = 0;
            }
        }
    }

    // ============ IDLE / ВЫБОР АТАКИ ============

    private void tickIdle() {
        vector.set(0, 0);

        if (currentPhase >= 5) {
            startComplexBarrage();
            return;
        }
        if (currentPhase >= 4) {
            startSimpleBarrage();
            return;
        }

        owner.summonedMinions.removeIf(e -> e.removed);

        if (!hasAliveMinions() && attacksSinceSummon >= scaledSummonInterval()) {
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

    // ============ СТРЕЛЬБА (обычная) ============

    private void startShoot() {
        state = State.SHOOT;
        timer = scaledShootDuration();
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
        int baseCount = switch (currentPhase) { case 1 -> 3; case 2 -> 5; default -> 7; };
        int count = aggression() > 0.8 ? baseCount + 1 : baseCount;

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
        timer = scaledChargeWindup();
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
            timer = scaledChargeMaxTicks();
            chargeHit = false;
            owner.setSpeedMultiplier(scaledChargeSpeedMult());
            vector.set(chargeDir);
        }
    }

    private void tickCharge() {
        vector.set(chargeDir);
        timer--;

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
        timer = scaledRecover();
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
        currentSummonDuration = scaledSummonDuration();
        state = State.SUMMON;
        timer = currentSummonDuration;
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        if (owner.model != null && owner.model.hasAnimation("summon"))
            owner.model.play("summon", true);
    }

    private void tickSummon() {
        vector.set(0, 0);
        if (--timer == currentSummonDuration / 2) performSummon();
        if (timer <= 0) {
            state = State.IDLE;
            timer = 0;
        }
    }

    private void performSummon() {
        int flyingCount   = currentPhase == 1 ? 1 : 2;
        int crossbowCount = currentPhase >= 2 ? 1 : 0;
        if (aggression() > 0.8) flyingCount++;

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

    // ============ SIMPLE BARRAGE (ФАЗА 4, 25–50%) ============

    private void startSimpleBarrage() {
        state = State.SIMPLE_BARRAGE;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);

        // Телепорт в центр
        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;
        owner.position.set(cx, cy);
        owner.prevPosition.set(cx, cy);
        owner.updateAABB();

        // Пауза перед первым залпом — игрок сориентируется после рёва
        this.simpleBarrageShotTimer = 20;
        this.simpleBarrageTicksElapsed = 0;

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickSimpleBarrage() {
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        // Держим босса в центре
        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;
        owner.position.set(cx, cy);
        owner.prevPosition.set(cx, cy);
        owner.updateAABB();

        // Фаза длится 10 сек — потом босс умирает
        if (++simpleBarrageTicksElapsed >= BARRAGE_DURATION_TICKS) {
            owner.setHealth(0);
            return;
        }

        if (--simpleBarrageShotTimer <= 0) {
            // Залп: 8 орбов одновременно в 8 направлений, случайный стартовый угол
            double volleyAngle = random.nextDouble() * 360.0;
            for (int i = 0; i < BARRAGE_SHOT_COUNT; i++) {
                double angleDeg = volleyAngle + i * (360.0 / BARRAGE_SHOT_COUNT);
                fireSingleOrb(angleDeg);
            }
            simpleBarrageShotTimer = SIMPLE_BARRAGE_VOLLEY_INTERVAL;
        }
    }

    // ============ COMPLEX BARRAGE (ФАЗА 5, <25%) ============

    private double barrageProgress() {
        double hpRatio = Math.clamp(
                owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH),
                0.0, 1.0);
        return Math.clamp((COMPLEX_BARRAGE_HP_THRESHOLD - hpRatio) / COMPLEX_BARRAGE_HP_THRESHOLD, 0.0, 1.0);
    }

    private int scaledComplexBarrageInterval() {
        return Math.max(COMPLEX_BARRAGE_INTERVAL_MIN,
                (int) (COMPLEX_BARRAGE_INTERVAL_BASE - (COMPLEX_BARRAGE_INTERVAL_BASE - COMPLEX_BARRAGE_INTERVAL_MIN) * barrageProgress()));
    }

    private double scaledComplexBarrageRotation() {
        return COMPLEX_BARRAGE_ROTATION_BASE + (COMPLEX_BARRAGE_ROTATION_MAX - COMPLEX_BARRAGE_ROTATION_BASE) * barrageProgress();
    }

    private int randomFlipInterval() {
        return COMPLEX_BARRAGE_FLIP_MIN_INTERVAL
                + random.nextInt(COMPLEX_BARRAGE_FLIP_MAX_INTERVAL - COMPLEX_BARRAGE_FLIP_MIN_INTERVAL + 1);
    }

    private void startComplexBarrage() {
        state = State.COMPLEX_BARRAGE;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);

        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;
        owner.position.set(cx, cy);
        owner.prevPosition.set(cx, cy);
        owner.updateAABB();

        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        this.barrageAngle = toPlayer.lengthSquared() > 0.01
                ? Math.toDegrees(Math.atan2(toPlayer.y, toPlayer.x))
                : random.nextDouble() * 360.0;

        this.barrageShotTimer = scaledComplexBarrageInterval() * 2;
        this.barrageTicksElapsed = 0;
        this.barrageDirection = random.nextBoolean() ? 1 : -1;
        this.barrageFlipIn = randomFlipInterval();

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickComplexBarrage() {
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;
        owner.position.set(cx, cy);
        owner.prevPosition.set(cx, cy);
        owner.updateAABB();

        if (++barrageTicksElapsed >= BARRAGE_DURATION_TICKS) {
            owner.setHealth(0);
            return;
        }

        if (--barrageShotTimer <= 0) {
            fireBarragePattern();

            barrageAngle = (barrageAngle + barrageDirection * scaledComplexBarrageRotation()) % 360.0;
            if (barrageAngle < 0) barrageAngle += 360.0;

            if (--barrageFlipIn <= 0) {
                if (random.nextFloat() < COMPLEX_BARRAGE_FLIP_CHANCE) {
                    barrageDirection = -barrageDirection;
                }
                barrageFlipIn = randomFlipInterval();
            }

            barrageShotTimer = scaledComplexBarrageInterval();
        }
    }

    private void fireBarragePattern() {
        double damage = owner.getAttributeValue(EnemyAttributes.DAMAGE) * BARRAGE_ORB_DAMAGE_MULT;

        for (int i = 0; i < BARRAGE_SHOT_COUNT; i++) {
            double angleDeg = barrageAngle + i * (360.0 / BARRAGE_SHOT_COUNT);
            double rad = Math.toRadians(angleDeg);
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(BARRAGE_ORB_SPEED);

            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position),
                    vel,
                    damage
            ));
        }
    }

    // ============ ОБЩЕЕ ============

    private void fireSingleOrb(double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(BARRAGE_ORB_SPEED);
        double damage = owner.getAttributeValue(EnemyAttributes.DAMAGE) * BARRAGE_ORB_DAMAGE_MULT;

        game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                new Vector2d(owner.position),
                vel,
                damage
        ));
    }

    // ============ УТИЛИТЫ ============

    private void clampToRoom(Vector2d pos) {
        double padding = 2.0;
        pos.x = Math.clamp(pos.x, roomBounds.getMinX() + padding, roomBounds.getMaxX() - padding);
        pos.y = Math.clamp(pos.y, roomBounds.getMinY() + padding, roomBounds.getMaxY() - padding);
    }
}