package stayablox.purpleevil;

import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundBuffer;

public class Sounds {
    public static final String SOUNDS_PATH = "assets/sounds/";

    public final SoundBuffer MAIN_MENU = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "main-menu.ogg"));
    public final SoundBuffer STEP = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "step.ogg"));
    public final SoundBuffer SWING = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "swing.ogg"));
    public final SoundBuffer SWING_HIT = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "swing-hit.ogg"));
    public final SoundBuffer PLAYER_DAMAGE = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "player-damage.ogg"));
}
