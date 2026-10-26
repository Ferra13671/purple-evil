package stayablox.purpleevil;

import com.ferra13671.jsonutils.model.JArray;
import com.ferra13671.jsonutils.model.JModel;
import com.ferra13671.jsonutils.model.JType;
import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Sounds implements GameInstance {
    public static final String SOUNDS_PATH = "assets/sounds/";

    public final Map<String, SoundBuffer> entityDamageSounds = new HashMap<>();

    public final SoundBuffer MAIN_MENU = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "main-menu.ogg"));
    public final SoundBuffer STEP = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "step.ogg"));
    public final SoundBuffer SWING = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "swing.ogg"));
    public final SoundBuffer SWING_HIT = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "swing-hit.ogg"));
    public final SoundBuffer LOBBY = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "lobby.ogg"));
    public final SoundBuffer DUNGEON = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "dungeon.ogg"));
    public final SoundBuffer BUY = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "buy.ogg"));
    public final SoundBuffer STAFF = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "staff.ogg"));

    public Sounds() {
        loadEntityDamageSounds();
    }

    private void loadEntityDamageSounds() {
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "damage/" + "atlas.json5")) {
            JArray array = JModel.read(new InputStreamReader(inputStream)).cast(JType.JARRAY, null);

            if (array != null)
                array.forEach(element -> element.consumeIfExtends(JType.STRING, s -> this.entityDamageSounds.put(
                        s,
                        Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH  + "damage/" + s + ".ogg"))
                )));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
