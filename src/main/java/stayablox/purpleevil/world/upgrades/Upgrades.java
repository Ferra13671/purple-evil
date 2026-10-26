package stayablox.purpleevil.world.upgrades;

import com.ferra13671.jsonutils.model.*;
import stayablox.purpleevil.world.attribute.modifier.AttributeModifier;
import stayablox.purpleevil.world.attribute.modifier.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Upgrades {
    public static final String UPGRADES_PATH = "assets/upgrade/";

    public final Map<String, Upgrade> upgrades = new HashMap<>();

    public Upgrades() {
        loadUpgrades();
    }

    private void loadUpgrades() {
        try(InputStream is1 = getClass().getClassLoader().getResourceAsStream(UPGRADES_PATH + "info.json5")) {
            JArray array = JModel.read(new InputStreamReader(is1)).cast(JType.JARRAY, null);

            for (JElement element : array) {
                String id = element.cast(JType.STRING, null);

                try(InputStream is2 = getClass().getClassLoader().getResourceAsStream(UPGRADES_PATH + id + ".json5")) {
                    JObject upgradeObject = JModel.read(new InputStreamReader(is2)).cast(JType.JOBJECT, null);

                    int price = upgradeObject.getElement("price").cast(JType.NUMBER, null).intValue();
                    UpgradeType type = UpgradeType.fromId(upgradeObject.getElement("type").cast(JType.STRING, null));
                    boolean spawning = upgradeObject.getElement("spawning").cast(JType.BOOLEAN, null);
                    Map<String, AttributeModifier> modifiers = new HashMap<>();

                    JObject modifiersObject = upgradeObject.getElement("modifiers").cast(JType.JOBJECT, null);
                    modifiersObject.asMap().forEach((name, modifierElement) -> {
                        JObject modifierObject = modifierElement.cast(JType.JOBJECT, null);

                        double amount = modifierObject.getElement("amount").cast(JType.NUMBER, null).doubleValue();
                        Operation operation = Operation.fromId(modifierObject.getElement("operation").cast(JType.STRING, null));

                        modifiers.put(name, new AttributeModifier(id, amount, operation));
                    });

                    this.upgrades.put(id, new Upgrade(id, price, type, spawning, modifiers));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
