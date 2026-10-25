package govno.utils;

import govno.GameInstance;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Builder
public class KeyBind implements GameInstance {
    public static final Map<KeyType, List<KeyBind>> keybinds = new EnumMap<>(KeyType.class) {{
        put(KeyType.Keyboard, new ArrayList<>());
        put(KeyType.Mouse, new ArrayList<>());
    }};

    @Getter
    private final int key;
    @Getter
    @NonNull
    private final KeyType keyType;
    @Getter
    @Setter
    private boolean pressed;

    public KeyBind register() {
        keybinds.get(this.keyType).add(this);

        return this;
    }

    public static void resetAll() {
        keybinds.values().forEach(l -> l.forEach(k -> k.setPressed(false)));
    }
}
