package stayablox.purpleevil.world.entity.input;

import stayablox.purpleevil.utils.AABB;
import stayablox.purpleevil.world.entity.EntityAttributes;
import stayablox.purpleevil.world.entity.SlimeBossEntity;
import stayablox.purpleevil.world.entity.SlimeBossOrbEntity;
import stayablox.purpleevil.world.room.Room;
import org.joml.Vector2d;

import java.util.Random;

public class SlimeBossInput extends Input {
    private enum State {
        IDLE,
        PHASE_1_FLY,
        PHASE_2_JUMP,
        PHASE_2_LAND
    }

    private static final int PHASE_1_DURATION = 160;
    private static final int PHASE_2_JUMP_DURATION = 40;
    private static final int PHASE_2_LAND_DURATION = 15;
    private static final float JUMP_SPEED_MULTIPLIER = 15f;
    private static final double LAND_DAMAGE = 40;

    private static final double LAND_DAMAGE_EXTRA_RADIUS = 0.5;

    private final SlimeBossEntity owner;
    private final AABB roomBounds;
    private final Random random = new Random();

    private State state = State.IDLE;
    private int timer = 0;
    private int shootCooldown = 0;

    private final Vector2d moveDir = new Vector2d();
    private final Vector2d jumpDir = new Vector2d();
    private final Vector2d jumpTarget = new Vector2d();

    public SlimeBossInput(SlimeBossEntity owner, Room room) {
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

        if (!playerInSameRoom && state != State.IDLE) {
            state = State.IDLE;
            vector.set(0, 0);
            owner.phasing = false;
            owner.setSpeedMultiplier(1f);
            owner.velocity.set(0, 0);
            return;
        }

        if (playerInSameRoom && state == State.IDLE) {
            startPhase1();
            return;
        }

        switch (state) {
            case PHASE_1_FLY -> tickPhase1();
            case PHASE_2_JUMP -> tickPhase2Jump();
            case PHASE_2_LAND -> tickPhase2Land();
        }
    }

    private void startPhase1() {
        state = State.PHASE_1_FLY;
        timer = PHASE_1_DURATION;
        shootCooldown = 20;
        moveDir.set(0, 0);
    }

    private void startJump() {
        jumpTarget.set(game.getGameSession().player.position);
        jumpDir.set(jumpTarget).sub(owner.position);

        if (jumpDir.lengthSquared() < 0.01) {
            state = State.PHASE_2_LAND;
            timer = PHASE_2_LAND_DURATION;
            return;
        }

        jumpDir.normalize();
        state = State.PHASE_2_JUMP;
        timer = PHASE_2_JUMP_DURATION;
        owner.phasing = true;
        owner.setSpeedMultiplier(JUMP_SPEED_MULTIPLIER);

        if (owner.model != null && owner.model.hasAnimation("attack"))
            owner.model.play("attack", true);
    }

    private void tickPhase1() {
        timer--;
        shootCooldown--;

        Vector2d playerPos = game.getGameSession().player.position;
        Vector2d toPlayer = new Vector2d(playerPos).sub(owner.position);
        double distanceToPlayer = toPlayer.length();

        double maxAllowedDistance = 15.0;

        if (distanceToPlayer > maxAllowedDistance) {
            // Если босс слишком далеко, он начинает преследовать игрока
            if (toPlayer.lengthSquared() > 0.01) {
                moveDir.set(toPlayer).normalize();
            }
        } else {
            // Если босс находится в пределах допустимой дистанции, он летает хаотично
            if (moveDir.lengthSquared() < 0.001 || random.nextInt(60) == 0) {
                int dir = random.nextInt(8);
                double angle = dir * (Math.PI / 4);
                moveDir.set(Math.cos(angle), Math.sin(angle));
            }
        }

        // Отталкивание от стен комнаты
        double padding = 2.5;
        double nextX = owner.position.x + moveDir.x;
        double nextY = owner.position.y + moveDir.y;
        if (nextX < roomBounds.getMinX() + padding || nextX > roomBounds.getMaxX() - padding)
            moveDir.x = -moveDir.x;
        if (nextY < roomBounds.getMinY() + padding || nextY > roomBounds.getMaxY() - padding)
            moveDir.y = -moveDir.y;

        vector.set(moveDir);

        if (shootCooldown <= 0) {
            shootByHealth();

            // Чем меньше ХП, тем чаще стрельба
            double maxHp = owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
            double hpRatio = owner.getHealth() / maxHp;

            // При 100% ХП - 15 тиков (0.75с), при 0% ХП - 5 тиков (0.25с)
            shootCooldown = (int) (5 + 10 * hpRatio);
        }

        if (timer <= 0) {
            startJump();
        }
    }

    private void shootByHealth() {
        double maxHp = owner.getAttributeValue(EntityAttributes.MAX_HEALTH);
        double hpRatio = owner.getHealth() / maxHp;
        float orbSpeed = 0.25f;

        if (hpRatio > 0.66) {
            // 1 Фаза: Одна пуля в сторону игрока
            Vector2d dir = new Vector2d(game.getGameSession().player.position).sub(owner.position);
            if (dir.lengthSquared() < 0.01) dir.set(1, 0); // Защита от нулевого вектора
            else dir.normalize();

            spawnOrb(dir.mul(orbSpeed));

        } else if (hpRatio > 0.33) {
            // 2 Фаза: Крест из 4 пуль, повернутый на рандомную точку возле игрока
            Vector2d targetPos = new Vector2d(game.getGameSession().player.position);
            // Смещение от -2 до +2 блоков по X и Y
            targetPos.add((random.nextDouble() * 4 - 2), (random.nextDouble() * 4 - 2));

            Vector2d dirToTarget = targetPos.sub(owner.position);
            double baseAngle = Math.toDegrees(Math.atan2(dirToTarget.y, dirToTarget.x));

            for (int i = 0; i < 4; i++) {
                float angle = (float) (baseAngle + i * 90f);
                Vector2d dir = new Vector2d(
                        Math.cos(Math.toRadians(angle)),
                        Math.sin(Math.toRadians(angle))
                ).normalize().mul(orbSpeed);
                spawnOrb(dir);
            }

        } else {
            // 3 Фаза: 8 пуль в 4 стороны и 4 диагонали
            float baseAngle = random.nextFloat() * 45f;
            for (int i = 0; i < 8; i++) {
                float angle = baseAngle + i * 45f;
                Vector2d dir = new Vector2d(
                        Math.cos(Math.toRadians(angle)),
                        Math.sin(Math.toRadians(angle))
                ).normalize().mul(orbSpeed);
                spawnOrb(dir);
            }
        }
    }

    private void spawnOrb(Vector2d velocity) {
        game.getWorld().currentRoom.entities.add(new SlimeBossOrbEntity(
                new Vector2d(owner.position),
                velocity,
                15
        ));
    }

    private void tickPhase2Jump() {
        vector.set(jumpDir);
        timer--;

        Vector2d toTarget = new Vector2d(jumpTarget).sub(owner.position);
        double distSq = toTarget.lengthSquared();
        boolean passed = distSq > 0 && toTarget.dot(jumpDir) < 0;

        if (distSq < 0.1 || passed || timer <= 0) {
            owner.position.set(jumpTarget);
            owner.updateAABB();

            state = State.PHASE_2_LAND;
            timer = PHASE_2_LAND_DURATION;
            owner.phasing = false;
            owner.setSpeedMultiplier(1f);
            vector.set(0, 0);
            owner.velocity.set(0, 0);

            Vector2d toPlayer = new Vector2d(game.getGameSession().player.position)
                    .sub(owner.position);
            if (toPlayer.length() <= (owner.modelAABB.getXLength() / 2) + LAND_DAMAGE_EXTRA_RADIUS) {
                game.getGameSession().player.damage(LAND_DAMAGE);
                game.getGameSession().player.applyKnockback(owner.position, 1.5);
            }
        }
    }

    private void tickPhase2Land() {
        vector.set(0, 0);
        timer--;

        if (timer <= 0) {
            startPhase1();
        }
    }
}