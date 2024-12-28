package net.pedroricardo.missingmods;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pedroricardo.missingmods.config.Mod;

import java.util.List;

public class MissingModsServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        List<Mod> requiredMods = MissingMods.CONFIG.required.get();
        boolean hasAllRequiredMods = true;
        for (Mod mod : requiredMods) {
            if (!mod.environment().matches(FabricLoader.getInstance().getEnvironmentType())) continue;
            if (!FabricLoader.getInstance().isModLoaded(mod.id()) || !mod.validVersions().test(FabricLoader.getInstance().getModContainer(mod.id()).get().getMetadata().getVersion())) {
                if (hasAllRequiredMods) {
                    MissingMods.LOGGER.error("Some required mods are missing!");
                    hasAllRequiredMods = false;
                }
                String version;
                if (mod.validVersions().toString().equals("*")) {
                    version = "any version";
                } else {
                    version = "version " + mod.validVersions();
                }
                MissingMods.LOGGER.info("Install " + mod.id() + ", " + version + ": " + mod.link());
                mod.reason().ifPresent(reason -> MissingMods.LOGGER.info("  - Reason: " + reason.getString()));
            }
        }
        List<Mod> optionalMods = MissingMods.CONFIG.optional.get();
        boolean hasAllOptionalMods = true;
        for (Mod mod : optionalMods) {
            if (!mod.environment().matches(FabricLoader.getInstance().getEnvironmentType())) continue;
            if (!FabricLoader.getInstance().isModLoaded(mod.id()) || !mod.validVersions().test(FabricLoader.getInstance().getModContainer(mod.id()).get().getMetadata().getVersion())) {
                if (hasAllOptionalMods) {
                    MissingMods.LOGGER.warn("Some optional mods are missing!");
                    hasAllOptionalMods = false;
                }
                String version;
                if (mod.validVersions().toString().equals("*")) {
                    version = "any version";
                } else {
                    version = "version " + mod.validVersions();
                }
                MissingMods.LOGGER.info("Install " + mod.id() + ", " + version + ": " + mod.link());
                mod.reason().ifPresent(reason -> MissingMods.LOGGER.info("  - Reason: " + reason.getString()));
            }
        }
        if (!hasAllRequiredMods) {
            System.exit(1);
        }
    }
}
