package govno.world.room;

import com.ferra13671.jsonutils.model.*;
import govno.world.block.BlockType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                return new RoomTemplate(name, blockTemplates, maxWidth, -y);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
