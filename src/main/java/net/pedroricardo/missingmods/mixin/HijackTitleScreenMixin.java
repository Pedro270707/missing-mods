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
            List<Mod> requiredMods = MissingMods.CONFIG.required.get();
            List<Mod> missingRequiredMods = new ArrayList<>();
            for (Mod mod : requiredMods) {
                if (!mod.environment().matches(FabricLoader.getInstance().getEnvironmentType())) continue;
                if (!FabricLoader.getInstance().isModLoaded(mod.id()) || !mod.validVersions().test(FabricLoader.getInstance().getModContainer(mod.id()).get().getMetadata().getVersion())) {
                    missingRequiredMods.add(mod);
                }
            }
            List<Mod> optionalMods = MissingMods.CONFIG.optional.get();
            List<Mod> missingOptionalMods = new ArrayList<>();
            for (Mod mod : optionalMods) {
                if (!mod.environment().matches(FabricLoader.getInstance().getEnvironmentType())) continue;
                if (!FabricLoader.getInstance().isModLoaded(mod.id()) || !mod.validVersions().test(FabricLoader.getInstance().getModContainer(mod.id()).get().getMetadata().getVersion())) {
                    missingOptionalMods.add(mod);
                }
            }
            MinecraftClient.getInstance().setScreen(new MissingModsScreen(missingRequiredMods, missingOptionalMods));
        }
    }
}
