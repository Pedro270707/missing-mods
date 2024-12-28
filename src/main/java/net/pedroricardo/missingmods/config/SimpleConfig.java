package net.pedroricardo.missingmods.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.pedroricardo.missingmods.MissingMods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleConfig {
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SimpleConfigLoader loader;

    private final File file;
    private final HashMap<String, CodecAndDefaultPair<?>> optionToCodec = new HashMap<>();
    private final HashMap<String, Object> options = new HashMap<>();

    public SimpleConfig(String location) {
        this.file = FabricLoader.getInstance().getConfigDir().resolve(location + ".json").toFile();
        this.loader = new SimpleConfigLoader(this.file);
    }

    public void reload() {
        JsonObject config = this.loader.load();
        this.options.clear();
        for (Map.Entry<String, CodecAndDefaultPair<?>> entry : this.optionToCodec.entrySet()) {
            CodecAndDefaultPair<?> pair = entry.getValue();
            JsonElement element = config.getAsJsonObject().get(entry.getKey());
            DataResult<? extends Pair<?, JsonElement>> result = entry.getValue().codec().decode(JsonOps.INSTANCE, element);
            if (element != null && result.error().isEmpty()) {
                this.options.put(entry.getKey(), result.get().orThrow().getFirst());
            } else {
                config.add(entry.getKey(), pair.encodeStart(JsonOps.INSTANCE).get().orThrow());
                this.options.put(entry.getKey(), pair.defaultValue());
            }
        };
        try (FileWriter fileWriter = new FileWriter(this.file)) {
            fileWriter.write(PRETTY_GSON.toJson(config));
        } catch (IOException ignored) {
            MissingMods.LOGGER.error("Could not write config to location " + this.file.getAbsolutePath());
        }
    }

    public <T> Supplier<T> register(String key, Codec<T> codec, T defaultValue) {
        this.optionToCodec.put(key, new CodecAndDefaultPair<>(codec, defaultValue));
        return () -> {
            Object value = this.options.get(key);
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        };
//        return () -> {
//            JsonElement element = this.config.getAsJsonObject().get(key);
//            DataResult<Pair<T, JsonElement>> result = codec.decode(JsonOps.INSTANCE, element);
//            T value = defaultValue;
//            if (element == null || result.error().isPresent()) {
//                this.config.getAsJsonObject().add(key, codec.encodeStart(JsonOps.INSTANCE, defaultValue).get().orThrow());
//            } else {
//                value = result.get().orThrow().getFirst();
//            }
//            try (FileWriter fileWriter = new FileWriter(this.file)) {
//                fileWriter.write(PRETTY_GSON.toJson(this.config));
//            } catch (IOException ignored) {
//                MissingMods.LOGGER.error("Could not write config option " + key + " to location " + this.file.getAbsolutePath());
//            }
//            return value;
//        };
    }

    record CodecAndDefaultPair<T>(Codec<T> codec, T defaultValue) {
        public <A> DataResult<A> encodeStart(DynamicOps<A> ops) {
            return this.codec().encodeStart(ops, this.defaultValue());
        }
    }
}
