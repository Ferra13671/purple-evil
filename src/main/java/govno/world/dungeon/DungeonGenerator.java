package govno.world.dungeon;

import govno.utils.Direction;
import govno.world.block.Block;
import govno.world.block.BlockType;
import govno.world.room.*;
import java.util.*;

public class DungeonGenerator {

    private final RoomTemplates templates;
    private final DungeonProperties properties;
    private final Random random;

    private final List<PlacedRoom> placed = new ArrayList<>();
    private final List<Room> tunnelRooms = new ArrayList<>();
    private final Set<Long> occupied = new HashSet<>();

    public DungeonGenerator(RoomTemplates templates, DungeonProperties properties, long seed) {
        this.templates = templates;
        this.properties = properties;
        this.random = new Random(seed);
    }

    // Изменили int distance на double distance (физическое расстояние)
    private record PlacedRoom(Room room, List<RoomExit> freeExits, double distance) { }

    public List<Room> generate() {
        placed.clear();
        tunnelRooms.clear();
        occupied.clear();

        placeRooms();
        placeExitRoom();
        sealUnusedExits();

        List<Room> result = new ArrayList<>();
        for (PlacedRoom pr : placed) result.add(pr.room);
        result.addAll(tunnelRooms);
        return result;
    }

    private void placeRooms() {
        RoomTemplate spawn = templates.templates.getOrDefault(
                "spawn", templates.templates.values().iterator().next());
        Room spawnRoom = new Room(spawn, 0, 0);

        // Для спавна дистанция 0
        placed.add(new PlacedRoom(spawnRoom, new ArrayList<>(spawnRoom.exits), 0.0));
        commitRoom(spawnRoom);

        // Генерируем на 1 комнату меньше, чтобы оставить место для выхода
        while (placed.size() < this.properties.maxRooms() - 1) {
            if (!tryPlaceNextRoom()) break;
        }
    }

    private void placeExitRoom() {
        RoomTemplate exitTemplate = templates.templates.get("exit");
        if (exitTemplate == null) {
            System.err.println("ВНИМАНИЕ: Шаблон 'exit' не найден в rooms.json5!");
            return;
        }

        // Сортируем комнаты по убыванию физического расстояния
        List<PlacedRoom> sortedRooms = new ArrayList<>(placed);
        sortedRooms.sort((a, b) -> Double.compare(b.distance, a.distance));

        for (PlacedRoom farthest : sortedRooms) {
            if (farthest.freeExits.isEmpty()) continue;

            if (tryPlaceSpecificFromSource(farthest, exitTemplate)) {
                return; // Успешно поставили
            }
        }
        System.err.println("Не удалось разместить комнату выхода!");
    }

    private boolean tryPlaceNextRoom() {
        List<PlacedRoom> candidates = placed.stream()
                .filter(pr -> !pr.freeExits.isEmpty()).toList();
        if (candidates.isEmpty()) return false;

        PlacedRoom last = placed.getLast();

        List<PlacedRoom> sources = new ArrayList<>(candidates);
        Collections.shuffle(sources, random);

        if (!last.freeExits.isEmpty() && random.nextDouble() >= this.properties.branchChance()) {
            sources.remove(last);
            sources.addFirst(last);
        }

        for (PlacedRoom source : sources) {
            if (tryPlaceFromSource(source)) return true;
        }
        return false;
    }

    private boolean tryPlaceFromSource(PlacedRoom source) {
        List<RoomExit> shuffledExits = new ArrayList<>(source.freeExits);
        Collections.shuffle(shuffledExits, random);

        for (RoomExit exitA : shuffledExits) {
            Direction needed = exitA.side().opposite();

            List<RoomTemplate> templateCandidates = new ArrayList<>();
            for (RoomTemplate t : templates.templates.values()) {
                if (t.roomType() == RoomType.Exit) continue; // Не ставим выход случайно
                for (RoomExit e : t.exits())
                    if (e.side() == needed) { templateCandidates.add(t); break; }
            }
            Collections.shuffle(templateCandidates, random);

            for (RoomTemplate tmpl : templateCandidates) {
                if (tryBuildAndPlace(source, exitA, tmpl, needed)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryPlaceSpecificFromSource(PlacedRoom source, RoomTemplate tmpl) {
        List<RoomExit> shuffledExits = new ArrayList<>(source.freeExits);
        Collections.shuffle(shuffledExits, random);

        for (RoomExit exitA : shuffledExits) {
            Direction needed = exitA.side().opposite();

            boolean hasDoor = false;
            for (RoomExit e : tmpl.exits()) {
                if (e.side() == needed) { hasDoor = true; break; }
            }
            if (!hasDoor) continue;

            if (tryBuildAndPlace(source, exitA, tmpl, needed)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryBuildAndPlace(PlacedRoom source, RoomExit exitA, RoomTemplate tmpl, Direction needed) {
        for (RoomExit exitBTemplate : tmpl.exits()) {
            if (exitBTemplate.side() != needed) continue;

            int[] center = computeNewCenter(exitA, exitBTemplate, tmpl.width(), tmpl.height());
            Room newRoom = new Room(tmpl, center[0], center[1]);
            if (overlapsAnything(newRoom)) continue;

            int halfW = tmpl.width() / 2;
            int halfH = tmpl.height() / 2;
            RoomExit exitBWorld = new RoomExit(
                    exitBTemplate.x() + center[0] - halfW,
                    exitBTemplate.y() + center[1] + halfH,
                    exitBTemplate.width(), exitBTemplate.height(),
                    exitBTemplate.side()
            );

            Room tunnel = buildTunnelRoom(exitA, exitBWorld);
            if (overlapsAnything(tunnel)) continue;

            // Вычисляем физическое расстояние от спавна
            Room spawnRoom = placed.getFirst().room;
            double dist = Math.sqrt(
                    Math.pow(newRoom.centerX - spawnRoom.centerX, 2) +
                            Math.pow(newRoom.centerY - spawnRoom.centerY, 2)
            );

            PlacedRoom newPlaced = new PlacedRoom(newRoom, new ArrayList<>(newRoom.exits), dist);
            newPlaced.freeExits.remove(exitBWorld);
            source.freeExits.remove(exitA);

            placed.add(newPlaced);
            tunnelRooms.add(tunnel);
            commitRoom(newRoom);
            commitRoom(tunnel);
            return true;
        }
        return false;
    }

    private void sealUnusedExits() {
        for (PlacedRoom pr : placed) {
            if (pr.freeExits.isEmpty()) continue;

            Set<Long> toSeal = new HashSet<>();
            for (RoomExit exit : pr.freeExits) {
                for (int dx = 0; dx < exit.width(); dx++)
                    for (int dy = 0; dy < exit.height(); dy++)
                        toSeal.add(key(exit.x() + dx, exit.y() + dy));
            }

            List<Block> newBlocks = new ArrayList<>(pr.room.blocks.size());
            for (Block b : pr.room.blocks) {
                if (toSeal.contains(key(b.x, b.y))) {
                    newBlocks.add(new Block(BlockType.SealedExit, b.x, b.y));
                } else {
                    newBlocks.add(b);
                }
            }
            pr.room.blocks.clear();
            pr.room.blocks.addAll(newBlocks);
        }
    }

    private int[] computeNewCenter(RoomExit exitA, RoomExit exitBTemplate, int tw, int th) {
        int halfW = tw / 2, halfH = th / 2;
        int targetBX, targetBY;
        switch (exitA.side()) {
            case RIGHT -> { targetBX = exitA.x() + exitA.width() + this.properties.tunnelLength(); targetBY = exitA.y(); }
            case LEFT  -> { targetBX = exitA.x() - this.properties.tunnelLength() - exitBTemplate.width(); targetBY = exitA.y(); }
            case UP    -> { targetBX = exitA.x(); targetBY = exitA.y() + exitA.height() + this.properties.tunnelLength(); }
            case DOWN  -> { targetBX = exitA.x(); targetBY = exitA.y() - this.properties.tunnelLength() - exitBTemplate.height(); }
            default -> throw new IllegalStateException();
        }
        return new int[]{
                targetBX - exitBTemplate.x() + halfW,
                targetBY - exitBTemplate.y() - halfH
        };
    }

    private static long key(int x, int y) { return ((long) x << 32) | (y & 0xFFFFFFFFL); }

    private boolean overlapsAnything(Room candidate) {
        for (Block b : candidate.blocks)
            if (occupied.contains(key(b.x, b.y))) return true;
        return false;
    }

    private void commitRoom(Room r) {
        for (Block b : r.blocks) occupied.add(key(b.x, b.y));
    }

    private Room buildTunnelRoom(RoomExit a, RoomExit b) {
        List<Block> blocks = new ArrayList<>();

        if (a.side() == Direction.LEFT || a.side() == Direction.RIGHT) {
            int xStart = Math.min(a.x() + a.width(), b.x() + b.width());
            int xEnd   = Math.max(a.x(), b.x()) - 1;
            int yMin   = Math.min(a.y(), b.y());
            int yMax   = Math.max(a.y() + a.height(), b.y() + b.height()) - 1;

            for (int x = xStart; x <= xEnd; x++) {
                for (int y = yMin; y <= yMax; y++)
                    blocks.add(new Block(BlockType.Floor, x, y));
                blocks.add(new Block(BlockType.Wall, x, yMin - 1));
                blocks.add(new Block(BlockType.Wall, x, yMax + 1));
            }
        } else {
            int yStart = Math.min(a.y() + a.height(), b.y() + b.height());
            int yEnd   = Math.max(a.y(), b.y()) - 1;
            int xMin   = Math.min(a.x(), b.x());
            int xMax   = Math.max(a.x() + a.width(), b.x() + b.width()) - 1;

            for (int y = yStart; y <= yEnd; y++) {
                for (int x = xMin; x <= xMax; x++)
                    blocks.add(new Block(BlockType.Floor, x, y));
                blocks.add(new Block(BlockType.Wall, xMin - 1, y));
                blocks.add(new Block(BlockType.Wall, xMax + 1, y));
            }
        }

        return new Room(blocks, RoomType.Tunnel);
    }
}