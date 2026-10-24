package govno.renderer;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class AtlasBuilder {
    private final String name;
    private final List<GLTexture> textures = new ArrayList<>();

    public AtlasBuilder(String name) {
        this.name = name;
    }

    public void texture(GLTexture texture) {
        this.textures.addLast(texture);
    }

    public TextureAtlas build() {
        return new TextureAtlas(this.name, this.textures);
    }
}
