package net.pedroricardo.missingmods;

import net.fabricmc.api.ModInitializer;
import net.pedroricardo.missingmods.config.MissingModsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingMods implements ModInitializer {
    public static final String MOD_ID = "missingmods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final MissingModsConfig CONFIG = new MissingModsConfig();

    @Override
    public void onInitialize() {
        CONFIG.reload();
    }
}
