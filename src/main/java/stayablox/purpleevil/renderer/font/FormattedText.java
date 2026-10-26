package stayablox.purpleevil.renderer.font;

import com.ferra13671.cometrenderer.utils.Pair;
import lombok.Getter;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FormattedText {
    @Getter
    private final List<Pair<Supplier<Vector4fc>, Character[]>> components = new ArrayList<>();

    public FormattedText(String s) {
        List<Character> characters = new ArrayList<>();

        for (char _char : s.toCharArray())
            characters.add(_char);

        if (!characters.isEmpty()) {
            this.components.add(new Pair<>(() -> new Vector4f(1f, 1f, 1f, 1f), characters.toArray(new Character[0])));
            characters.clear();
        }
    }

    public FormattedText append(String text) {
        return append(new FormattedText(text));
    }

    public FormattedText append(FormattedText formattedText) {
        this.components.addAll(formattedText.getComponents());

        return this;
    }

    public FormattedText append(Supplier<Vector4fc> color, String text) {
        this.components.add(new Pair<>(color, text.chars().mapToObj(c -> (char) c).toList().toArray(new Character[0])));

        return this;
    }
}
