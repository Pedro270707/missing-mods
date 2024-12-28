package net.pedroricardo.missingmods.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SimpleConfigLoader {
    private final File file;

    public SimpleConfigLoader(File file) {
        this.file = file;
    }

    public JsonObject load() {
        try {
            if (!this.file.exists() && !this.file.createNewFile()) {
                return new JsonObject();
            }
            JsonElement config = new JsonObject();
            try {
                config = JsonParser.parseReader(new FileReader(this.file));
            } catch (IOException ignored) {
            }
            if (!config.isJsonObject()) {
                config = new JsonObject();
            }
            return (JsonObject) config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
