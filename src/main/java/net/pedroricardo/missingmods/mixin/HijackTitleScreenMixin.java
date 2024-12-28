package net.pedroricardo.missingmods.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.pedroricardo.missingmods.MissingMods;
import net.pedroricardo.missingmods.client.MissingModsScreen;
import net.pedroricardo.missingmods.config.Mod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(TitleScreen.class)
@Environment(EnvType.CLIENT)
public class HijackTitleScreenMixin {
    @Unique
    private static boolean hasSeenMissingModsScreen = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void missingmods$hijackTitleScreen(CallbackInfo ci) {
        if (!hasSeenMissingModsScreen) {
            hasSeenMissingModsScreen = true;
            List<Mod> missingRequiredMods = MissingMods.getMissingMods(MissingMods.CONFIG.required.get());
            List<Mod> missingOptionalMods = MissingMods.getMissingMods(MissingMods.CONFIG.optional.get());
            if (!missingRequiredMods.isEmpty() || !missingOptionalMods.isEmpty()) {
                MinecraftClient.getInstance().setScreen(new MissingModsScreen(missingRequiredMods, missingOptionalMods));
            }
        }
    }
}
