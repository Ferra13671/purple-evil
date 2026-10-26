package govno.utils;

import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class KeyNames {
    private final Map<Integer, String> KEYBOARD_NAMES = new HashMap<>();
    private final Map<Integer, String> MOUSE_NAMES = new HashMap<>();

    static {
        populateKeyboard();
        populateMouse();
    }

    private void populateKeyboard() {
        for (Field field : GLFW.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int.class)
                continue;

            String name = field.getName();
            if (!name.startsWith("GLFW_KEY_")) continue;

            if (name.equals("GLFW_KEY_LAST") || name.equals("GLFW_KEY_UNKNOWN"))
                continue;

            try {
                int code = field.getInt(null);
                String display = prettifyKeyName(name.substring("GLFW_KEY_".length()));
                KEYBOARD_NAMES.putIfAbsent(code, display);
            } catch (IllegalAccessException ignored) {}
        }
    }

    private void populateMouse() {
        for (Field field : GLFW.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int.class)
                continue;

            String name = field.getName();
            if (!name.startsWith("GLFW_MOUSE_BUTTON_")) continue;

            String suffix = name.substring("GLFW_MOUSE_BUTTON_".length());
            if (suffix.equals("LAST") || !suffix.matches("\\d+")) continue;

            try {
                int code = field.getInt(null);
                MOUSE_NAMES.put(code, "M" + suffix);
            } catch (IllegalAccessException ignored) {}
        }

        for (Field field : GLFW.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int.class)
                continue;

            String name = field.getName();
            if (!name.startsWith("GLFW_MOUSE_BUTTON_")) continue;

            String suffix = name.substring("GLFW_MOUSE_BUTTON_".length());
            if (suffix.matches("\\d+") || suffix.equals("LAST")) continue;

            try {
                int code = field.getInt(null);
                MOUSE_NAMES.put(code, prettifyMouseName(suffix));
            } catch (IllegalAccessException ignored) {}
        }
    }

    private String prettifyKeyName(String name) {
        if (name.startsWith("KP_")) {
            String rest = name.substring(3);
            return switch (rest) {
                case "DECIMAL"  -> "NUM.";
                case "DIVIDE"   -> "NUM/";
                case "MULTIPLY" -> "NUM*";
                case "SUBTRACT" -> "NUM-";
                case "ADD"      -> "NUM+";
                case "ENTER"    -> "NUM ENT";
                case "EQUAL"    -> "NUM=";
                default         -> "NUM " + rest;
            };
        }

        return switch (name) {
            case "ESCAPE"          -> "ESC";
            case "BACKSPACE"       -> "BACK";
            case "DELETE"          -> "DEL";
            case "INSERT"          -> "INS";
            case "PAGE_UP"         -> "PGUP";
            case "PAGE_DOWN"       -> "PGDN";
            case "CAPS_LOCK"       -> "CAPS";
            case "SCROLL_LOCK"     -> "SCROLL";
            case "NUM_LOCK"        -> "NUMLOCK";
            case "PRINT_SCREEN"    -> "PRTSC";
            case "GRAVE_ACCENT"    -> "`";
            case "APOSTROPHE"      -> "'";
            case "BACKSLASH"       -> "\\";
            case "LEFT_BRACKET"    -> "[";
            case "RIGHT_BRACKET"   -> "]";
            case "SEMICOLON"       -> ";";
            case "LEFT_SHIFT"      -> "LSHIFT";
            case "RIGHT_SHIFT"     -> "RSHIFT";
            case "LEFT_CONTROL"    -> "LCTRL";
            case "RIGHT_CONTROL"   -> "RCTRL";
            case "LEFT_ALT"        -> "LALT";
            case "RIGHT_ALT"       -> "RALT";
            case "LEFT_SUPER"      -> "LWIN";
            case "RIGHT_SUPER"     -> "RWIN";
            default                -> name;
        };
    }

    private String prettifyMouseName(String name) {
        return switch (name) {
            case "LEFT"   -> "LMB";
            case "RIGHT"  -> "RMB";
            case "MIDDLE" -> "MMB";
            default       -> name;
        };
    }

    public String ofKeyboard(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "?";
        return KEYBOARD_NAMES.getOrDefault(key, "?" + key);
    }

    public String ofMouse(int button) {
        return MOUSE_NAMES.getOrDefault(button, "M" + button);
    }

    public static String of(KeyType type, int code) {
        return type == KeyType.Mouse ? ofMouse(code) : ofKeyboard(code);
    }
}