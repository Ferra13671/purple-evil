package govno.model;

import com.ferra13671.cometrenderer.texture.GLTexture;
import com.ferra13671.cometrenderer.texture.TextureFiltering;
import com.ferra13671.cometrenderer.texture.loader.TextureLoader;
import com.ferra13671.jsonutils.model.*;
import govno.GameInstance;
import govno.utils.Direction;
import org.joml.Vector2f;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityModelTemplates implements GameInstance {
    public static final String ENTITY_TEXTURES_PATH = "assets/textures/entity/";

    public final Map<String, EntityModelTemplate> templates = new HashMap<>();

    public EntityModelTemplates() {
        loadTemplates();
    }

    private void loadTemplates() {
        try(InputStream is1 = getClass().getClassLoader().getResourceAsStream(ENTITY_TEXTURES_PATH + "atlas.json5")) {
            JArray atlas = JModel.read(new InputStreamReader(is1)).cast(JType.JARRAY, null);

            for (JElement e1 : atlas) {
                String modelName = e1.cast(JType.STRING, null);

                try(InputStream is2 = getClass().getClassLoader().getResourceAsStream(ENTITY_TEXTURES_PATH + modelName + "/atlas.json5")) {
                    JArray model = JModel.read(new InputStreamReader(is2)).cast(JType.JARRAY, null);

                    Map<Direction, Map<String, Animation>> animations = Map.of(
                            Direction.UP, new HashMap<>(),
                            Direction.DOWN, new HashMap<>(),
                            Direction.LEFT, new HashMap<>(),
                            Direction.RIGHT, new HashMap<>()
                    );

                    for (JElement e2 : model) {
                        JObject animationObject = e2.cast(JType.JOBJECT, null);

                        JArray framesArray = animationObject.getElement("frames").cast(JType.JARRAY, new JArray());

                        String animationName = animationObject.getElement("name").cast(JType.STRING, null);
                        boolean loop = animationObject.getElement("loop").cast(JType.BOOLEAN, false);
                        boolean directional = animationObject.getElement("directional").cast(JType.BOOLEAN, false);

                        animations.get(Direction.UP).put(animationName, parseAnim(modelName, animationName, loop, directional ? Direction.UP : null, framesArray));
                        animations.get(Direction.DOWN).put(animationName, parseAnim(modelName, animationName, loop, directional ? Direction.DOWN : null, framesArray));
                        animations.get(Direction.LEFT).put(animationName, parseAnim(modelName, animationName, loop, directional ? Direction.LEFT : null, framesArray));
                        animations.get(Direction.RIGHT).put(animationName, parseAnim(modelName, animationName, loop, directional ? Direction.RIGHT : null, framesArray));
                    }

                    this.templates.put(
                            modelName,
                            new EntityModelTemplate(modelName, animations)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Animation parseAnim(String modelName, String animationName, boolean loop, Direction direction, JArray framesArray) {
        List<Animation.Frame> frames = new ArrayList<>();

        for (JElement frameElement : framesArray) {
            JObject frameObject = frameElement.cast(JType.JOBJECT, null);

            String id = frameObject.getElement("id").cast(JType.STRING, null);
            float duration = frameObject.getElement("duration").cast(JType.NUMBER, null).floatValue();
            JArray sizeArr = frameObject.getElement("size").cast(JType.JARRAY, new JArray());
            Vector2f size = sizeArr.size() >= 2 ? new Vector2f(
                    sizeArr.get(0).cast(JType.NUMBER, null).floatValue(),
                    sizeArr.get(1).cast(JType.NUMBER, null).floatValue()
            ) : new Vector2f(1);

            String spriteName = modelName + "-" + animationName + (direction != null ? ("-" + direction.id) : "") + (!id.isEmpty() ? "-" + id : "");
            if (game.getTextures().getEntityTexture(spriteName) == null) {
                String texturePath = ENTITY_TEXTURES_PATH + modelName + "/" + animationName + (direction != null ? ("-" + direction.id) : "") + (!id.isEmpty() ? "-" + id : "") + ".png";
                try {
                    game.getTextures().addEntityTexture(GLTexture.builder(TextureLoader.IN_JAR)
                            .name(spriteName)
                            .info(texturePath)
                            .filtering(TextureFiltering.DEFAULT)
                            .build()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Cannot load texture '%s' from anim '%s'", texturePath, animationName), e);
                }
            }

            frames.addLast(new Animation.Frame(
                    new Sprite(
                            spriteName,
                            size.x,
                            size.y
                    ),
                    duration
            ));
        }

        return new Animation(animationName, loop, frames);
    }
}
