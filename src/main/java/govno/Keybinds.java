package govno;

import govno.utils.KeyBind;
import govno.utils.KeyType;
import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

@UtilityClass
public class Keybinds {
    public final KeyBind ATTACK = KeyBind.builder()
            .keyType(KeyType.Mouse)
            .key(GLFW.GLFW_MOUSE_BUTTON_LEFT)
            .build()
            .register();
    public final KeyBind FORWARD = KeyBind.builder()
            .keyType(KeyType.Keyboard)
            .key(GLFW.GLFW_KEY_W)
            .build()
            .register();
    public final KeyBind BACK = KeyBind.builder()
            .keyType(KeyType.Keyboard)
            .key(GLFW.GLFW_KEY_S)
            .build()
            .register();
    public final KeyBind LEFT = KeyBind.builder()
            .keyType(KeyType.Keyboard)
            .key(GLFW.GLFW_KEY_A)
            .build()
            .register();
    public final KeyBind RIGHT = KeyBind.builder()
            .keyType(KeyType.Keyboard)
            .key(GLFW.GLFW_KEY_D)
            .build()
            .register();
    public final KeyBind PLAYER_SWAP = KeyBind.builder()
            .keyType(KeyType.Keyboard)
            .key(GLFW.GLFW_KEY_R)
            .build()
            .register();
}
