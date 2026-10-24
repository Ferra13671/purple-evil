package govno;

import com.ferra13671.overtone.Overtone;
import com.ferra13671.overtone.SoundBuffer;

public class Sounds {
    public static final String SOUNDS_PATH = "assets/sounds/";

    public final SoundBuffer MAIN_MENU = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "main-menu.ogg"));
    public final SoundBuffer WALK = Overtone.createBuffer(Overtone.OGG_DECODER, getClass().getClassLoader().getResourceAsStream(SOUNDS_PATH + "walk.ogg"));
}
