package govno.world.room;

import com.ferra13671.jsonutils.model.*;
import govno.utils.Direction;
import govno.world.block.BlockType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class RoomTemplates {
    public static final String ROOMS_PATH = "assets/world/rooms/";

    public final Map<String, RoomTemplate> templates = new HashMap<>();

    public RoomTemplates() {
        parseTemplates();
    }

    private void parseTemplates() {
        JArray rooms = JModel.read(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(ROOMS_PATH + "rooms.json5"))).cast(JType.JARRAY, null);
        for (JElement element : rooms) {
            String id = element.cast(JType.STRING, null);

            this.templates.put(id, parseTemplate(getClass().getClassLoader().getResourceAsStream(ROOMS_PATH + id + ".json5")));
        }
    }

    private RoomTemplate parseTemplate(InputStream stream) {
        try(InputStreamReader reader = new InputStreamReader(stream)) {
            JObject object = JModel.read(reader).cast(JType.JOBJECT, null);

            if (object != null) {
                String name = object.getElement("name").cast(JType.STRING, null);
                List<BlockTemplate> blockTemplates = new ArrayList<>();

                int maxWidth = 0;
                int x = 0, y = 0;
                JArray grid = object.castElement("grid", JType.JARRAY, null);
                for (JElement element : grid) {
                    String s = element.cast(JType.STRING, null);
                    for (char ch : s.toCharArray()) {
                        BlockType blockType = BlockType.fromId(ch);

                        if (blockType != BlockType.Void)
                            blockTemplates.add(new BlockTemplate(x, y, blockType));

                        x++;
                    }

                    y--;
                    if (x > maxWidth)
                        maxWidth = x;
                    x = 0;
                }

                int width = maxWidth;
                int height = -y;

                List<RoomExit> exits = computeExits(blockTemplates, width, height);

                return new RoomTemplate(name, blockTemplates, exits, width, height);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<RoomExit> computeExits(List<BlockTemplate> blockTemplates, int width, int height) {
        List<BlockTemplate> exitBlocks = blockTemplates.stream()
                .filter(t -> t.blockType() == BlockType.Exit)
                .toList();

        List<RoomExit> exits = new ArrayList<>();
        Set<BlockTemplate> unvisited = new HashSet<>(exitBlocks);

        while (!unvisited.isEmpty()) {
            BlockTemplate start = unvisited.iterator().next();
            unvisited.remove(start);

            int minX = start.x(), maxX = start.x();
            int minY = start.y(), maxY = start.y();

            Queue<BlockTemplate> queue = new LinkedList<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                BlockTemplate current = queue.poll();
                minX = Math.min(minX, current.x());
                maxX = Math.max(maxX, current.x());
                minY = Math.min(minY, current.y());
                maxY = Math.max(maxY, current.y());

                Iterator<BlockTemplate> it = unvisited.iterator();
                while (it.hasNext()) {
                    BlockTemplate other = it.next();
                    if (Math.abs(current.x() - other.x()) + Math.abs(current.y() - other.y()) == 1) {
                        it.remove();
                        queue.add(other);
                    }
                }
            }

            int w = maxX - minX + 1;
            int h = maxY - minY + 1;
            Direction side = detectSide(minX, minY, maxX, maxY, width, height);
            exits.add(new RoomExit(minX, minY, w, h, side));
        }

        return exits;
    }

    private Direction detectSide(int minX, int minY, int maxX, int maxY, int width, int height) {
        if (minX == maxX) {
            if (minX == 0)          return Direction.LEFT;
            if (minX == width - 1)  return Direction.RIGHT;
        }
        if (minY == maxY) {
            if (maxY == 0)            return Direction.UP;
            if (minY == -(height - 1)) return Direction.DOWN;
        }
        throw new IllegalArgumentException(
                "Exit has unsupported shape or isn't on the boundary: " +
                        "[" + minX + ".." + maxX + "] x [" + minY + ".." + maxY + "]"
        );
    }
}
