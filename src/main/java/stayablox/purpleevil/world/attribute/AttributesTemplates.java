package stayablox.purpleevil.world.attribute;

import com.ferra13671.jsonutils.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AttributesTemplates {
    public final String ATTRIBUTES_TEMPLATES_PATH = "assets/attributes/";

    public final Map<String, Map<String, Double>> templates = new HashMap<>();

    public AttributesTemplates() {
        loadTemplates();
    }

    private void loadTemplates() {
        try(InputStream is1 = getClass().getClassLoader().getResourceAsStream(ATTRIBUTES_TEMPLATES_PATH + "info.json5")) {
            JArray array = JModel.read(new InputStreamReader(is1)).cast(JType.JARRAY, null);

            for (JElement element : array) {
                String id = element.cast(JType.STRING, null);

                try(InputStream is2 = getClass().getClassLoader().getResourceAsStream(ATTRIBUTES_TEMPLATES_PATH + id + ".json5")) {
                    JObject attributesObject = JModel.read(new InputStreamReader(is2)).cast(JType.JOBJECT, null);

                    Map<String, Double> attributes = new HashMap<>();

                    attributesObject.asMap().forEach((name, value) -> attributes.put(name, value.cast(JType.NUMBER, null).doubleValue()));

                    this.templates.put(id, attributes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
