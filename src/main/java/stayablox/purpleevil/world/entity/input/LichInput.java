package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.*;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class LichInput extends Input {
    private enum State {
        SUMMON,
        SHOOT,
        TELEPORT_IN,
        ATTACK,
        TELEPORT_OUT,
        BERSERK_CHARGE
    }

    // ===== Конфигурация фазы берсерка =====
    private static final double BERSERK_HP_THRESHOLD = 0.30;
    private static final int    BERSERK_CHARGE_COUNT = 4;
    private static final int    BERSERK_CHARGE_MAX_TICKS = 50;
    private static final double BERSERK_TELEPORT_MIN_DISTANCE = 7.0;
    private static final float  BERSERK_SPEED_MULTIPLIER = 7.0f;
    private static final double BERSERK_HIT_RADIUS = 1.8;
    private static final double BERSERK_DAMAGE_MULTIPLIER = 1.5;
    private static final double BERSERK_KNOCKBACK = 2.0;

    private final LichBossEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.SUMMON;
    private int timer = 0;
    private int shootCooldown = 0;
    private boolean hasHit = false;

    // ===== Состояние берсерка =====
    private int berserkChargesLeft = 0;
    private int berserkTimer = 0;
    private boolean berserkHit = false;
    private final Vector2d berserkDir = new Vector2d();

    public LichInput(LichBossEntity owner, Room room) {
        this.owner = owner;
        this.roomBounds = room.getBounds();
    }

    @Override
    public void tick() {
        if (game.getWorld() == null || game.getWorld().currentRoom == null) {
            vector.set(0, 0);
            return;
        }

        boolean playerInSameRoom = game.getWorld().currentRoom.entities.contains(owner);
        if (!playerInSameRoom) {
            vector.set(0, 0);
            return;
        }

        if (shootCooldown > 0) shootCooldown--;

        owner.summonedMinions.removeIf(e -> e.removed);

        switch (state) {
            case SUMMON -> tickSummon();
            case SHOOT -> tickShoot();
            case TELEPORT_IN -> tickTeleportIn();
            case ATTACK -> tickAttack();
            case TELEPORT_OUT -> tickTeleportOut();
            case BERSERK_CHARGE -> tickBerserkCharge();
        }
    }

    // ============ СУЩЕСТВУЮЩИЕ МЕТОДЫ ============

    private void tickSummon() {
        vector.set(0, 0);

        if (timer == 0) {
            if (owner.model != null && owner.model.hasAnimation("summon"))
                owner.model.play("summon", true);
            timer = 40;
        } else {
            timer--;
            if (timer == 1) {
                double hpRatio = owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);

                int count = 3;
                List<Function<Room, EnemyEntity>> pool = new ArrayList<>();

                if (hpRatio > 0.66) {
                    count = 3;
                    pool.add(DefaultSkeletonEntity::new);
                } else if (hpRatio > 0.33) {
                    count = 5;
                    pool.add(DefaultSkeletonEntity::new);
                    pool.add(DefaultSkeletonEntity::new);
                    pool.add(KillerSkeletonEntity::new);
                } else {
                    count = 7;
                    pool.add(DefaultSkeletonEntity::new);
                    pool.add(KillerSkeletonEntity::new);
                    pool.add(KnightSkeletonEntity::new);
                }

                for (int i = 0; i < count; i++) {
                    Function<Room, EnemyEntity> creator = pool.get(random.nextInt(pool.size()));
                    EnemyEntity minion = creator.apply(game.getWorld().currentRoom);
                    minion.canDropUpgrades = false;
                    minion.position.set(owner.position).add(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
                    minion.updateAABB();
                    game.getWorld().currentRoom.entities.add(minion);
                    owner.summonedMinions.add(minion);
                }

                state = State.TELEPORT_IN;
                timer = 10;
                if (owner.model != null && owner.model.hasAnimation("teleport"))
                    owner.model.play("teleport", true);
            }
        }
    }

    private void tickShoot() {
        vector.set(0, 0);

        if (shootCooldown <= 0) {
            Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                game.getWorld().currentRoom.entities.add(new SkeletonOrbEntity(
                        new Vector2d(owner.position),
                        new Vector2d(toPlayer).mul(0.3),
                        owner.getAttributeValue(EnemyAttributes.DAMAGE) / 2.0
                ));
                shootCooldown = 20;
            }
        }

        if (timer-- <= 0) {
            state = State.SUMMON;
            timer = 0;
        }
    }

    private void tickTeleportIn() {
        vector.set(0, 0);

        if (timer-- <= 0) {
            Vector2d playerPos = game.getGameSession().player.position;
            Vector2d playerVel = game.getGameSession().player.velocity;

            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 3.5 + random.nextDouble() * 1.5;
            double offsetX = Math.cos(angle) * distance;
            double offsetY = Math.sin(angle) * distance;

            double predictX = playerVel.x * 10;
            double predictY = playerVel.y * 10;

            Vector2d target = new Vector2d(
                    playerPos.x + offsetX + predictX,
                    playerPos.y + offsetY + predictY
            );
            clampToRoom(target);

            owner.position.set(target);
            owner.prevPosition.set(target);
            owner.updateAABB();

            state = State.ATTACK;
            timer = 30;
            hasHit = false;

            if (owner.model != null && owner.model.hasAnimation("attack"))
                owner.model.play("attack", true);
        }
    }

    private void tickAttack() {
        Vector2d toPlayer = new Vector2d(game.getGameSession().player.position).sub(owner.position);
        double distance = toPlayer.length();

        if (!hasHit && distance <= 1.5) {
            hasHit = true;
            game.getGameSession().player.damage(owner.getAttributeValue(EnemyAttributes.DAMAGE));
            game.getGameSession().player.applyKnockback(owner.position, 1.0);
            vector.set(0, 0);
        } else if (!hasHit) {
            if (toPlayer.lengthSquared() > 0.01) {
                toPlayer.normalize();
                vector.set(toPlayer);
            }
        } else {
            vector.set(0, 0);
        }

        if (timer-- <= 0) {
            state = State.TELEPORT_OUT;
            timer = 20;
            if (owner.model != null && owner.model.hasAnimation("teleport"))
                owner.model.play("teleport", true);
        }
    }

    private void tickTeleportOut() {
        vector.set(0, 0);

        if (timer-- <= 0) {
            Vector2d playerPos = game.getGameSession().player.position;
            Vector2d target = new Vector2d(owner.position);

            boolean noMinions = owner.summonedMinions.isEmpty();

            if (noMinions && isBerserkReady()) {
                startBerserkPhase();
                return;
            }

            boolean nextStateIsShoot = noMinions;

            if (nextStateIsShoot) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = 4.0 + random.nextDouble() * 2.0;
                target.set(
                        playerPos.x + Math.cos(angle) * distance,
                        playerPos.y + Math.sin(angle) * distance
                );
            } else {
                int attempts = 0;
                while (target.distance(playerPos) < 5.0 && attempts < 10) {
                    target.set(
                            roomBounds.getMinX() + random.nextDouble() * roomBounds.getXLength(),
                            roomBounds.getMinY() + random.nextDouble() * roomBounds.getYLength()
                    );
                    attempts++;
                }
            }

            clampToRoom(target);

            owner.position.set(target);
            owner.prevPosition.set(target);
            owner.updateAABB();

            if (nextStateIsShoot) {
                state = State.SHOOT;
                timer = 80;
                shootCooldown = 0;
            } else {
                state = State.TELEPORT_IN;
                timer = 10;
                if (owner.model != null && owner.model.hasAnimation("teleport"))
                    owner.model.play("teleport", true);
            }
        }
    }

    // ============ ФАЗА БЕРСЕРКА ============

    private boolean isBerserkReady() {
        double hpRatio = owner.getHealth() / owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        return hpRatio < BERSERK_HP_THRESHOLD;
    }

    private void startBerserkPhase() {
        berserkChargesLeft = BERSERK_CHARGE_COUNT;
        startBerserkTeleport();
    }

    /**
     * Телепорт в случайную точку комнаты вдали от игрока.
     * Без замаха — сразу же начинаем рывок в том же тике.
     */
    private void startBerserkTeleport() {
        Vector2d playerPos = game.getGameSession().player.position;
        Vector2d target = new Vector2d();

        int attempts = 0;
        do {
            target.set(
                    roomBounds.getMinX() + random.nextDouble() * roomBounds.getXLength(),
                    roomBounds.getMinY() + random.nextDouble() * roomBounds.getYLength()
            );
            attempts++;
        } while (target.distance(playerPos) < BERSERK_TELEPORT_MIN_DISTANCE && attempts < 30);

        clampToRoom(target);

        owner.position.set(target);
        owner.prevPosition.set(target);
        owner.updateAABB();
        owner.velocity.set(0, 0);

        if (owner.model != null && owner.model.hasAnimation("teleport"))
            owner.model.play("teleport", true);

        // Сразу переходим к рывку — без паузы
        startBerserkCharge();
    }

    /**
     * Фиксируем текущую позицию игрока и начинаем таран.
     */
    private void startBerserkCharge() {
        Vector2d target = game.getGameSession().player.position;
        berserkDir.set(target).sub(owner.position);

        if (berserkDir.lengthSquared() < 0.01) {
            double angle = random.nextDouble() * Math.PI * 2;
            berserkDir.set(Math.cos(angle), Math.sin(angle));
        } else {
            berserkDir.normalize();
        }

        state = State.BERSERK_CHARGE;
        berserkTimer = BERSERK_CHARGE_MAX_TICKS;
        berserkHit = false;

        owner.phasing = true;
        owner.setSpeedMultiplier(BERSERK_SPEED_MULTIPLIER);

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickBerserkCharge() {
        vector.set(berserkDir);
        berserkTimer--;

        if (!berserkHit) {
            double distToPlayer = new Vector2d(game.getGameSession().player.position)
                    .sub(owner.position).length();

            if (distToPlayer <= BERSERK_HIT_RADIUS) {
                berserkHit = true;
                game.getGameSession().player.damage(
                        owner.getAttributeValue(EnemyAttributes.DAMAGE) * BERSERK_DAMAGE_MULTIPLIER);
                game.getGameSession().player.applyKnockback(owner.position, BERSERK_KNOCKBACK);
            }
        }

        if (berserkTimer <= 0) {
            endBerserkCharge();
        }
    }

    private void endBerserkCharge() {
        owner.phasing = false;
        owner.setSpeedMultiplier(1f);
        owner.velocity.set(0, 0);
        vector.set(0, 0);

        clampToRoom(owner.position);
        owner.prevPosition.set(owner.position);
        owner.updateAABB();

        berserkChargesLeft--;

        if (berserkChargesLeft > 0) {
            startBerserkTeleport();
        } else {
            state = State.SUMMON;
            timer = 0;
        }
    }

    // ============ УТИЛИТЫ ============

    private void clampToRoom(Vector2d pos) {
        double padding = 1.5;
        pos.x = Math.clamp(pos.x, roomBounds.getMinX() + padding, roomBounds.getMaxX() - padding);
        pos.y = Math.clamp(pos.y, roomBounds.getMinY() + padding, roomBounds.getMaxY() - padding);
    }
}