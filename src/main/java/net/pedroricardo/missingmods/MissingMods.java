package net.pedroricardo.missingmods;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;
import net.pedroricardo.missingmods.config.MissingModsConfig;
import net.pedroricardo.missingmods.config.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingMods implements ModInitializer {
    public static final String MOD_ID = "missingmods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MissingModsConfig CONFIG = new MissingModsConfig();

    @Override
    public void onInitialize() {
        CONFIG.reload();
        System.out.println(CONFIG.required.get());
        System.out.println(CONFIG.optional.get());
        JsonObject object = new JsonObject();
        object.addProperty("id", "deeperdarker");
        object.addProperty("link", "https://curseforge.com/minecraft/mc-mods/deeperdarker/files");
        object.addProperty("valid_versions", ">=1.3.3");

        System.out.println(Mod.CODEC.decode(JsonOps.INSTANCE, object).get().orThrow().getFirst());
        System.out.println(Mod.CODEC.encodeStart(JsonOps.INSTANCE, Mod.CODEC.decode(JsonOps.INSTANCE, object).get().orThrow().getFirst()).get().orThrow());
    }
}
