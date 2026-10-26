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
        FLY_TO_CENTER,
        COMPLEX_BARRAGE,
        SIMPLE_BARRAGE
    }

    // ===== Базовые тайминги =====
    private static final int TRANSITION_DURATION    = 20;   // было 30 — короче рёв
    private static final int CHARGE_WINDUP_DURATION = 15;
    private static final int CHARGE_MAX_TICKS       = 25;
    private static final int SUMMON_DURATION        = 40;
    private static final int SUMMON_INTERVAL        = 3;    // было 4 — чаще призыв

    // ===== Рывок =====
    private static final double CHARGE_DAMAGE_MULT   = 1.5;
    private static final double CHARGE_KNOCKBACK     = 2.5;
    private static final double CHARGE_HIT_RADIUS    = 2.0;
    private static final int    CHARGE_PREDICT_TICKS = 12;  // предсказание позиции игрока
    private static final int    CHARGE_CHAIN_WINDUP  = 8;   // короткий замах между серийными рывками

    // ===== Орбы во время замаха рывка =====
    private static final int    WINDUP_ORB_INTERVAL    = 5;
    private static final double WINDUP_ORB_DAMAGE_MULT = 0.4;
    private static final double WINDUP_ORB_SPEED       = 0.35;

    // ===== Шоквейв после последнего рывка в серии =====
    private static final int    SHOCKWAVE_ORB_COUNT   = 8;
    private static final double SHOCKWAVE_DAMAGE_MULT = 0.4;
    private static final double SHOCKWAVE_ORB_SPEED   = 0.3;

    // ===== Стрельба: несколько залпов =====
    private static final int SHOOT_VOLLEYS_MIN = 2;
    private static final int SHOOT_VOLLEYS_MAX = 3;
    private static final int SHOOT_VOLLEY_GAP  = 8;

    // ===== Enrage (HP < 10%) =====
    private static final double ENRAGE_HP_THRESHOLD = 0.10;
    private static final float  ENRAGE_TIME_MULT    = 0.65f;
    private static final float  ENRAGE_SPEED_MULT   = 1.3f;

    // ===== Barrage-фазы =====
    private static final int    BARRAGE_SHOT_COUNT       = 8;
    private static final double BARRAGE_ORB_SPEED        = 0.35;
    private static final double BARRAGE_ORB_DAMAGE_MULT  = 0.5;
    private static final int    BARRAGE_DURATION_TICKS   = 200;
    private static final int    ATTACKS_BETWEEN_BARRAGES = 2;   // было 3 — чаще

    // ===== Сложная карусель (P5, <25%) =====
    private static final double COMPLEX_BARRAGE_HP_THRESHOLD      = 0.25;
    private static final int    COMPLEX_BARRAGE_INTERVAL_BASE     = 5;      // было 3
    private static final int    COMPLEX_BARRAGE_INTERVAL_MIN      = 3;      // было 1
    private static final double COMPLEX_BARRAGE_ROTATION_BASE     = 1.0;
    private static final double COMPLEX_BARRAGE_ROTATION_MAX      = 2.0;
    private static final int    COMPLEX_BARRAGE_FLIP_MIN_INTERVAL = 8;
    private static final int    COMPLEX_BARRAGE_FLIP_MAX_INTERVAL = 20;
    private static final float  COMPLEX_BARRAGE_FLIP_CHANCE       = 0.55f;
    private static final double COMPLEX_BARRAGE_ORB_SPEED         = 0.55;

    // ===== Стрельба в режиме ожидания миньёнов (не путать с обычной) =====
    private static final int    MINION_WAIT_SHOOT_INTERVAL = 30;    // 1.5 сек между мини-залпами
    private static final double MINION_WAIT_ORB_SPEED      = 0.3;   // медленнее обычного

    // ===== Простая (P4, <50%) =====
    private static final double SIMPLE_BARRAGE_HP_THRESHOLD    = 0.50;
    private static final int    SIMPLE_BARRAGE_VOLLEY_INTERVAL = 8;   // было 10

    // ===== Долёт к центру =====
    private static final float  FLY_TO_CENTER_SPEED_MULT = 3.0f;
    private static final double FLY_TO_CENTER_SNAP_DIST  = 1.0;

    private final DemonBossEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.IDLE;
    private int currentPhase = 1;
    private int timer = 0;
    private int attackCounter = 0;
    private int attacksSinceSummon = 0;
    private int attacksSinceBarrage = 0;

    private int currentSummonDuration = SUMMON_DURATION;

    // ===== Рывок / цепочки =====
    private final Vector2d chargeDir = new Vector2d();
    private boolean chargeHit = false;
    private int chargesRemaining = 0;
    private int windupOrbTimer = 0;
    private int minionWaitShotTimer = 0;

    // ===== Multi-volley стрельба =====
    private int volleysRemaining = 0;

    // ===== P5 state =====
    private double barrageAngle = 0;
    private int barrageShotTimer = 0;
    private int barrageDirection = 1;
    private int barrageFlipIn = 0;
    private int complexBarrageTicksElapsed = 0;

    // ===== P4 state =====
    private int simpleBarrageShotTimer = 0;
    private int simpleBarrageTicksElapsed = 0;

    private int pendingBarragePhase = 0;

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
            case FLY_TO_CENTER -> tickFlyToCenter();
            case COMPLEX_BARRAGE -> tickComplexBarrage();
            case SIMPLE_BARRAGE -> tickSimpleBarrage();
        }
    }

    // ============ ENRAGE / АГРЕССИЯ ============

    private boolean isEnraged() {
        return hpRatio() < ENRAGE_HP_THRESHOLD;
    }

    private int enrageTime(int base) {
        return isEnraged() ? Math.max(1, (int) (base * ENRAGE_TIME_MULT)) : base;
    }

    private float enrageSpeed(float base) {
        return isEnraged() ? base * ENRAGE_SPEED_MULT : base;
    }

    private double aggression() {
        double maxHp = owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        if (maxHp <= 0) return 0.0;
        double hpRatio = Math.clamp(owner.getHealth() / maxHp, 0.0, 1.0);
        return 1.0 - hpRatio;
    }

    private double hpRatio() {
        return owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
    }

    private int scaledChargeWindup() {
        int base = Math.max(6, (int) (CHARGE_WINDUP_DURATION - 9 * aggression()));
        return enrageTime(base);
    }

    private int scaledChargeMaxTicks() {
        int base = (int) (CHARGE_MAX_TICKS + 10 * aggression());
        return enrageTime(base);
    }

    private int scaledRecover() {
        int phaseBase = switch (currentPhase) { case 1 -> 20; case 2 -> 15; default -> 10; };
        int base = Math.max(5, (int) (phaseBase - 8 * aggression()));
        return enrageTime(base);
    }

    private float scaledChargeSpeedMult() {
        float phaseBase = switch (currentPhase) { case 1 -> 4.0f; case 2 -> 5.0f; default -> 6.0f; };
        float base = phaseBase + 2.0f * (float) aggression();
        return enrageSpeed(base);
    }

    private int scaledSummonInterval() {
        int base = Math.max(2, (int) Math.round(SUMMON_INTERVAL - 2 * aggression()));
        return enrageTime(base);
    }

    private int scaledSummonDuration() {
        int base = Math.max(24, (int) (SUMMON_DURATION - 16 * aggression()));
        return enrageTime(base);
    }

    // ============ ФАЗЫ ============

    private int computePhase() {
        double ratio = owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        if (ratio < COMPLEX_BARRAGE_HP_THRESHOLD) return 5;
        if (ratio < SIMPLE_BARRAGE_HP_THRESHOLD) return 4;
        if (ratio < 0.66) return 2;
        return 1;
    }

    private void startTransition() {
        state = State.TRANSITION;
        timer = enrageTime(TRANSITION_DURATION);
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);

        double secs = timer * game.getTicker().getTickDelay() / 1000.0;
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
            // Если миньёны живы — пропускаем barrage и уходим в shoot
            if (hasAliveMinions()) {
                state = State.IDLE;
                timer = 0;
                attackCounter = 0;
                attacksSinceSummon = 0;
                return;
            }

            attacksSinceBarrage = 0;
            if (currentPhase >= 5) {
                beginBarragePhase(5);
            } else if (currentPhase >= 4) {
                beginBarragePhase(4);
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

        // Пока живы миньёны — редкий слабый залп, а не полноценный SHOOT
        if (hasAliveMinions()) {
            if (minionWaitShotTimer > 0) {
                minionWaitShotTimer--;
                return;
            }
            fireMinionWaitVolley();
            minionWaitShotTimer = enrageTime(MINION_WAIT_SHOOT_INTERVAL);
            return;
        }

        if (attacksSinceBarrage >= ATTACKS_BETWEEN_BARRAGES) {
            if (currentPhase >= 5) {
                attacksSinceBarrage = 0;
                beginBarragePhase(5);
                return;
            }
            if (currentPhase >= 4) {
                attacksSinceBarrage = 0;
                beginBarragePhase(4);
                return;
            }
        }

        owner.summonedMinions.removeIf(e -> e.removed);

        if (!hasAliveMinions() && attacksSinceSummon >= scaledSummonInterval()) {
            attacksSinceSummon = 0;
            attacksSinceBarrage++;
            attackCounter++;
            startSummon();
            return;
        }

        attacksSinceSummon++;
        attacksSinceBarrage++;
        attackCounter++;
        if (attackCounter % 2 == 1) {
            startShoot();
        } else {
            startChargeChain();
        }
    }

    private void fireMinionWaitVolley() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        if (toPlayer.lengthSquared() < 0.01) return;
        double baseAngle = Math.toDegrees(Math.atan2(toPlayer.y, toPlayer.x));
        double dmg = owner.getAttributeValue(EnemyAttributes.DAMAGE);

        // Всегда 3 орба в узком спреде, независимо от фазы
        for (int offset : new int[]{-20, 0, 20}) {
            double rad = Math.toRadians(baseAngle + offset);
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(MINION_WAIT_ORB_SPEED);
            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position), vel, dmg));
        }

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private boolean hasAliveMinions() {
        for (Entity e : owner.summonedMinions) {
            if (e.removed) continue;
            if (e instanceof LivingEntity le && le.isDying()) continue;
            return true;
        }
        return false;
    }

    // ============ СТРЕЛЬБА (multi-volley) ============

    private void startShoot() {
        state = State.SHOOT;
        volleysRemaining = SHOOT_VOLLEYS_MIN + random.nextInt(SHOOT_VOLLEYS_MAX - SHOOT_VOLLEYS_MIN + 1);
        timer = 0;  // первый залп — на следующем тике
        vector.set(0, 0);
    }

    private void tickShoot() {
        vector.set(0, 0);
        if (--timer <= 0) {
            if (volleysRemaining > 0) {
                fireOrbSpread();
                volleysRemaining--;
                timer = enrageTime(SHOOT_VOLLEY_GAP);
            } else {
                state = State.IDLE;
                timer = 0;
            }
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

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    // ============ РЫВОК (серии) ============

    private int getChargeChainCount() {
        double hp = hpRatio();
        if (hp < COMPLEX_BARRAGE_HP_THRESHOLD) return isEnraged() ? 4 : 3;
        if (hp < SIMPLE_BARRAGE_HP_THRESHOLD)  return 2;
        return 1;
    }

    private void startChargeChain() {
        chargesRemaining = getChargeChainCount();
        startChargeWindup();
    }

    private void startChargeWindup() {
        state = State.CHARGE_WINDUP;
        timer = scaledChargeWindup();
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        windupOrbTimer = WINDUP_ORB_INTERVAL;
        aimAtPredictedPlayer();

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void aimAtPredictedPlayer() {
        Vector2d playerPos = game.getGameSession().player.position;
        Vector2d playerVel = game.getGameSession().player.velocity;
        double predictTicks = timer + CHARGE_PREDICT_TICKS;
        Vector2d predicted = new Vector2d(playerPos).add(
                new Vector2d(playerVel).mul(predictTicks));

        Vector2d toPredicted = predicted.sub(owner.position);
        if (toPredicted.lengthSquared() < 0.01) {
            double a = random.nextDouble() * Math.PI * 2;
            chargeDir.set(Math.cos(a), Math.sin(a));
        } else {
            chargeDir.set(toPredicted).normalize();
        }
    }

    private void tickChargeWindup() {
        vector.set(0, 0);

        if (--windupOrbTimer <= 0) {
            fireWindupOrbs();
            windupOrbTimer = enrageTime(WINDUP_ORB_INTERVAL);
        }

        if (--timer <= 0) {
            state = State.CHARGE;
            timer = scaledChargeMaxTicks();
            chargeHit = false;
            owner.setSpeedMultiplier(scaledChargeSpeedMult());
            vector.set(chargeDir);
        }
    }

    private void fireWindupOrbs() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        if (toPlayer.lengthSquared() < 0.01) return;
        double baseAngle = Math.toDegrees(Math.atan2(toPlayer.y, toPlayer.x));
        double dmg = owner.getAttributeValue(EnemyAttributes.DAMAGE) * WINDUP_ORB_DAMAGE_MULT;

        for (int offset : new int[]{-20, 0, 20}) {
            double rad = Math.toRadians(baseAngle + offset);
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(WINDUP_ORB_SPEED);
            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position), vel, dmg));
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

        chargesRemaining--;

        if (chargesRemaining > 0) {
            // Серия продолжается: короткий замах → следующий рывок
            state = State.CHARGE_WINDUP;
            timer = enrageTime(CHARGE_CHAIN_WINDUP);
            windupOrbTimer = WINDUP_ORB_INTERVAL;
            aimAtPredictedPlayer();
        } else {
            spawnPostChargeShockwave();
            state = State.RECOVER;
            timer = scaledRecover();
        }
    }

    private void spawnPostChargeShockwave() {
        double dmg = owner.getAttributeValue(EnemyAttributes.DAMAGE) * SHOCKWAVE_DAMAGE_MULT;
        for (int i = 0; i < SHOCKWAVE_ORB_COUNT; i++) {
            double rad = Math.toRadians(i * (360.0 / SHOCKWAVE_ORB_COUNT));
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(SHOCKWAVE_ORB_SPEED);
            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position), vel, dmg));
        }
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
        if (isEnraged()) flyingCount++;

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

        minionWaitShotTimer = MINION_WAIT_SHOOT_INTERVAL;
    }

    // ============ ДОЛЁТ К ЦЕНТРУ ============

    private void beginBarragePhase(int phase) {
        this.pendingBarragePhase = phase;

        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;

        double dx = cx - owner.position.x;
        double dy = cy - owner.position.y;
        double distSq = dx * dx + dy * dy;

        if (distSq < FLY_TO_CENTER_SNAP_DIST * FLY_TO_CENTER_SNAP_DIST) {
            owner.position.set(cx, cy);
            owner.prevPosition.set(cx, cy);
            owner.velocity.set(0, 0);
            owner.updateAABB();
            activateBarragePhase(phase);
            return;
        }

        state = State.FLY_TO_CENTER;
        vector.set(dx, dy).normalize();
        owner.setSpeedMultiplier(FLY_TO_CENTER_SPEED_MULT);

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickFlyToCenter() {
        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;

        double dx = cx - owner.position.x;
        double dy = cy - owner.position.y;
        double distSq = dx * dx + dy * dy;

        if (distSq < FLY_TO_CENTER_SNAP_DIST * FLY_TO_CENTER_SNAP_DIST) {
            owner.position.set(cx, cy);
            owner.prevPosition.set(cx, cy);
            owner.velocity.set(0, 0);
            owner.setSpeedMultiplier(1f);
            owner.updateAABB();
            activateBarragePhase(pendingBarragePhase);
            return;
        }

        vector.set(dx, dy).normalize();
    }

    private void activateBarragePhase(int phase) {
        if (phase >= 5) activateComplexBarrage();
        else activateSimpleBarrage();
    }

    // ============ SIMPLE BARRAGE (P4) ============

    private void activateSimpleBarrage() {
        state = State.SIMPLE_BARRAGE;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);
        owner.updateAABB();

        this.simpleBarrageShotTimer = enrageTime(20);
        this.simpleBarrageTicksElapsed = 0;

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickSimpleBarrage() {
        vector.set(0, 0);
        owner.velocity.set(0, 0);

        double cx = (roomBounds.getMinX() + roomBounds.getMaxX()) / 2.0;
        double cy = (roomBounds.getMinY() + roomBounds.getMaxY()) / 2.0;
        owner.position.set(cx, cy);
        owner.prevPosition.set(cx, cy);
        owner.updateAABB();

        if (++simpleBarrageTicksElapsed >= BARRAGE_DURATION_TICKS) {
            state = State.IDLE;
            timer = 0;
            attacksSinceBarrage = 0;
            simpleBarrageTicksElapsed = 0;
            return;
        }

        if (--simpleBarrageShotTimer <= 0) {
            double volleyAngle = random.nextDouble() * 360.0;
            for (int i = 0; i < BARRAGE_SHOT_COUNT; i++) {
                double angleDeg = volleyAngle + i * (360.0 / BARRAGE_SHOT_COUNT);
                fireSingleOrb(angleDeg);
            }
            simpleBarrageShotTimer = enrageTime(SIMPLE_BARRAGE_VOLLEY_INTERVAL);
        }
    }

    // ============ COMPLEX BARRAGE (P5) ============

    private double barrageProgress() {
        double hpRatio = Math.clamp(
                owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH),
                0.0, 1.0);
        return Math.clamp((COMPLEX_BARRAGE_HP_THRESHOLD - hpRatio) / COMPLEX_BARRAGE_HP_THRESHOLD, 0.0, 1.0);
    }

    private int scaledComplexBarrageInterval() {
        int base = Math.max(COMPLEX_BARRAGE_INTERVAL_MIN,
                (int) (COMPLEX_BARRAGE_INTERVAL_BASE - (COMPLEX_BARRAGE_INTERVAL_BASE - COMPLEX_BARRAGE_INTERVAL_MIN) * barrageProgress()));
        return enrageTime(base);
    }

    private double scaledComplexBarrageRotation() {
        return COMPLEX_BARRAGE_ROTATION_BASE + (COMPLEX_BARRAGE_ROTATION_MAX - COMPLEX_BARRAGE_ROTATION_BASE) * barrageProgress();
    }

    private int randomFlipInterval() {
        return COMPLEX_BARRAGE_FLIP_MIN_INTERVAL
                + random.nextInt(COMPLEX_BARRAGE_FLIP_MAX_INTERVAL - COMPLEX_BARRAGE_FLIP_MIN_INTERVAL + 1);
    }

    private void activateComplexBarrage() {
        state = State.COMPLEX_BARRAGE;
        vector.set(0, 0);
        owner.velocity.set(0, 0);
        owner.setSpeedMultiplier(1f);
        owner.updateAABB();

        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        this.barrageAngle = toPlayer.lengthSquared() > 0.01
                ? Math.toDegrees(Math.atan2(toPlayer.y, toPlayer.x))
                : random.nextDouble() * 360.0;

        this.barrageShotTimer = scaledComplexBarrageInterval() * 2;
        this.barrageDirection = random.nextBoolean() ? 1 : -1;
        this.barrageFlipIn = randomFlipInterval();
        this.complexBarrageTicksElapsed = 0;

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

        if (++complexBarrageTicksElapsed >= BARRAGE_DURATION_TICKS) {
            state = State.IDLE;
            timer = 0;
            attacksSinceBarrage = 0;
            complexBarrageTicksElapsed = 0;
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
            Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(COMPLEX_BARRAGE_ORB_SPEED);
            game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                    new Vector2d(owner.position), vel, damage));
        }
    }

    // ============ ОБЩЕЕ ============

    private void fireSingleOrb(double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        Vector2d vel = new Vector2d(Math.cos(rad), Math.sin(rad)).mul(BARRAGE_ORB_SPEED);
        double damage = owner.getAttributeValue(EnemyAttributes.DAMAGE) * BARRAGE_ORB_DAMAGE_MULT;

        game.getWorld().currentRoom.entities.add(new DemonOrbEntity(
                new Vector2d(owner.position), vel, damage));
    }

    private void clampToRoom(Vector2d pos) {
        double padding = 2.0;
        pos.x = Math.clamp(pos.x, roomBounds.getMinX() + padding, roomBounds.getMaxX() - padding);
        pos.y = Math.clamp(pos.y, roomBounds.getMinY() + padding, roomBounds.getMaxY() - padding);
    }
}