package net.pedroricardo.missingmods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pedroricardo.missingmods.config.MissingModsConfig;
import net.pedroricardo.missingmods.config.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MissingMods implements ModInitializer {
    public static final String MOD_ID = "missingmods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MissingModsConfig CONFIG = new MissingModsConfig();

    @Override
    public void onInitialize() {
        CONFIG.reload();
    }

    public static List<Mod> getMissingMods(List<Mod> mods) {
        List<Mod> missing = new ArrayList<>();
        for (Mod mod : mods) {
            if (!mod.environment().matches(FabricLoader.getInstance().getEnvironmentType())) continue;
            if (!FabricLoader.getInstance().isModLoaded(mod.id()) || !mod.validVersions().test(FabricLoader.getInstance().getModContainer(mod.id()).get().getMetadata().getVersion())) {
                missing.add(mod);
            }
        }
        return missing;
    }
}
