package govno.renderer;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.TextureFiltering;
import com.ferra13671.cometrenderer.texture.atlas.TextureAtlas;
import com.ferra13671.cometrenderer.texture.loader.TextureLoader;
import com.ferra13671.jsonutils.model.JArray;
import com.ferra13671.jsonutils.model.JModel;
import com.ferra13671.jsonutils.model.JType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Textures {
    public static final String ROOM_TEXTURES_PATH = "assets/textures/world/";
    public static final String WEAPON_TEXTURES_PATH = "assets/textures/weapon/";

    private final Map<String, GLTexture> worldTextures = new HashMap<>();
    private final Map<String, GLTexture> entityTextures = new HashMap<>();
    private final Map<String, GLTexture> weaponTextures = new HashMap<>();

    public final TextureAtlas worldAtlas;
    private final AtlasBuilder entityAtlasBuilder = new AtlasBuilder("entity-atlas");
    public TextureAtlas entityAtlas;
    public TextureAtlas weaponAtlas;

    public Textures() {
        this.worldAtlas = loadRoomAtlas();
        this.weaponAtlas = loadWeaponAtlas();
    }

    public GLTexture getWorldTexture(String id) {
        return this.worldTextures.get(id);
    }

    public GLTexture getEntityTexture(String id) {
        return this.entityTextures.get(id);
    }

    public GLTexture getWeaponTexture(String id) {
        return this.weaponTextures.get(id);
    }

    public void addEntityTexture(GLTexture texture) {
        this.entityAtlasBuilder.texture(texture);
        this.entityTextures.put(texture.getName(), texture);
    }

    public void buildAtlases() {
        this.entityAtlas = entityAtlasBuilder.build();
        this.entityAtlas.setFiltering(TextureFiltering.DEFAULT);
    }

    private TextureAtlas loadRoomAtlas() {
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(ROOM_TEXTURES_PATH + "atlas.json5")) {
            JArray array = JModel.read(new InputStreamReader(inputStream)).cast(JType.JARRAY, null);

            if (array != null)
                array.forEach(element -> element.consumeIfExtends(JType.STRING, s -> this.worldTextures.put(s, GLTexture.builder(TextureLoader.IN_JAR).info(ROOM_TEXTURES_PATH + s + ".png").name(s).filtering(TextureFiltering.DEFAULT).build())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextureAtlas atlas = new TextureAtlas("world-atlas", this.worldTextures.values().stream().toList());
        atlas.setFiltering(TextureFiltering.DEFAULT);
        return atlas;
    }

    private TextureAtlas loadWeaponAtlas() {
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(WEAPON_TEXTURES_PATH + "atlas.json5")) {
            JArray array = JModel.read(new InputStreamReader(inputStream)).cast(JType.JARRAY, null);

            if (array != null)
                array.forEach(element -> element.consumeIfExtends(JType.STRING, s -> this.weaponTextures.put(s, GLTexture.builder(TextureLoader.IN_JAR).info(WEAPON_TEXTURES_PATH + s + ".png").name(s).filtering(TextureFiltering.DEFAULT).build())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextureAtlas atlas = new TextureAtlas("weapon-atlas", this.weaponTextures.values().stream().toList());
        atlas.setFiltering(TextureFiltering.DEFAULT);
        return atlas;
    }
}
