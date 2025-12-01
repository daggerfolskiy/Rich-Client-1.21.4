package fun.rich.utils.client.managers.file.impl;

import antidaunleak.api.annotation.Native;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fun.rich.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import fun.rich.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry;
import fun.rich.display.screens.clickgui.components.implement.autobuy.settings.AutoBuySettingsManager;
import fun.rich.utils.client.managers.file.ClientFile;
import fun.rich.utils.client.managers.file.exception.FileLoadException;
import fun.rich.utils.client.managers.file.exception.FileSaveException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AutoBuyConfigFile extends ClientFile {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AutoBuyConfigFile() {
        super("AutoBuy/AutoBuyConfig");
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        File autoBuyDir = new File(path.getParentFile(), "AutoBuy");
        if (!autoBuyDir.exists()) {
            autoBuyDir.mkdirs();
        }
        
        File file = new File(autoBuyDir, "AutoBuyConfig.json");
        if (!file.exists()) {
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                if (json.has("enabled_items")) {
                    JsonObject enabledItems = json.getAsJsonObject("enabled_items");
                    for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
                        if (enabledItems.has(item.getDisplayName())) {
                            item.setEnabled(enabledItems.get(item.getDisplayName()).getAsBoolean());
                        }
                    }
                }
                
                if (json.has("settings")) {
                    JsonObject settings = json.getAsJsonObject("settings");
                    AutoBuySettingsManager.getInstance().loadFromJson(settings);
                    ItemRegistry.reloadSettings();
                }
            }
        } catch (IOException e) {
            throw new FileLoadException("Failed to load AutoBuyConfig from file", e);
        }
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        File autoBuyDir = new File(path.getParentFile(), "AutoBuy");
        if (!autoBuyDir.exists()) {
            autoBuyDir.mkdirs();
        }
        
        JsonObject json = new JsonObject();
        
        JsonObject enabledItems = new JsonObject();
        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
            enabledItems.addProperty(item.getDisplayName(), item.isEnabled());
        }
        json.add("enabled_items", enabledItems);
        
        JsonObject settings = AutoBuySettingsManager.getInstance().saveToJson();
        json.add("settings", settings);
        
        File file = new File(autoBuyDir, "AutoBuyConfig.json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            throw new FileSaveException("Failed to save AutoBuyConfig to file", e);
        }
    }
}