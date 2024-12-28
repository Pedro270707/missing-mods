package net.pedroricardo.missingmods;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pedroricardo.missingmods.config.Mod;

import java.util.List;

public class MissingModsServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        List<Mod> missingRequiredMods = MissingMods.getMissingMods(MissingMods.CONFIG.required.get());
        if (!missingRequiredMods.isEmpty()) {
            MissingMods.LOGGER.warn("Some required mods are missing!");
        }
        missingRequiredMods.forEach(MissingModsServer::logMissingMod);

        List<Mod> missingOptionalMods = MissingMods.getMissingMods(MissingMods.CONFIG.optional.get());
        if (!missingOptionalMods.isEmpty()) {
            MissingMods.LOGGER.warn("Some optional mods are missing!");
        }
        missingOptionalMods.forEach(MissingModsServer::logMissingMod);

        if (!missingRequiredMods.isEmpty()) {
            System.exit(1);
        }
    }

    private static void logMissingMod(Mod mod) {
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
