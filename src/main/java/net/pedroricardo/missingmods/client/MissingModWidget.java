package net.pedroricardo.missingmods.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonStreamParser;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.realms.util.TextRenderingUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.pedroricardo.missingmods.config.Mod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MissingModWidget extends ClickableWidget {
    private static final Text DOWNLOAD_TEXT = Text.translatable("missingmods.mod.download").formatted(Formatting.BLUE, Formatting.UNDERLINE);

    private final Mod mod;
    private final TextRenderer textRenderer;
    private final Screen parent;

    public MissingModWidget(int x, int y, int width, int height, Mod mod, TextRenderer textRenderer, Screen parent) {
        super(x, y, width, height, Text.translatable("missingmods.mod", Text.literal(mod.id()).formatted(Formatting.YELLOW), mod.validVersions()));
        this.mod = mod;
        this.textRenderer = textRenderer;
        this.parent = parent;
    }

    public MissingModWidget(int width, Mod mod, TextRenderer textRenderer, Screen parent) {
        this(0, 0, width, textRenderer.fontHeight + 8, mod, textRenderer, parent);
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean modInstalled = this.checkForMod();
        if (modInstalled || !this.isSelected()) {
            Text message = modInstalled ? this.getMessage().copy().formatted(Formatting.STRIKETHROUGH) : this.getMessage();
            int color = modInstalled ? 0x99FFFFFF : 0xFFFFFFFF;
            context.drawText(this.textRenderer, message, this.getX() + 4, this.getY() + 5, color, true);
            if (modInstalled) {
                context.drawTexture(new Identifier("textures/gui/checkmark.png"), this.getX() + 9 + this.textRenderer.getWidth(message), this.getY() + 5, 0.0f, 0.0f, 9, 8, 9, 8);
            }
        } else {
            context.drawText(this.textRenderer, DOWNLOAD_TEXT, this.getX() + 4, this.getY() + 5, 0xFFFFFFFF, true);
        }
        if (this.isHovered() && this.mod.reason().isPresent()) {
            context.drawTooltip(this.textRenderer, Tooltip.of(this.mod.reason().get()).getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.checkForMod()) {
            MinecraftClient.getInstance().setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(this.mod.link());
                }
                MinecraftClient.getInstance().setScreen(parent);
            }, this.mod.link(), true));
            super.onClick(mouseX, mouseY);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    private boolean checkForMod() {
        if (FabricLoader.getInstance().isModLoaded(this.mod.id())) {
            return this.mod.validVersions().test(FabricLoader.getInstance().getModContainer(this.mod.id()).get().getMetadata().getVersion());
        }
        File[] files = ((FabricLoaderImpl) FabricLoader.getInstance()).getModsDirectory().listFiles();
        if (files == null) return false;
        for (File file : files) {
            if (!file.isFile() || !file.getAbsolutePath().endsWith(".jar")) {
                continue;
            }
            try (ZipFile zipFile = new ZipFile(file.getAbsolutePath())) {
                ZipEntry entry = zipFile.getEntry("fabric.mod.json");
                if (entry == null) {
                    continue;
                }
                InputStream stream = zipFile.getInputStream(entry);
                JsonElement element = JsonParser.parseReader(new JsonReader(new InputStreamReader(stream)));
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject metadata = element.getAsJsonObject();
                if (!metadata.has("id") || !metadata.get("id").getAsString().equals(this.mod.id())) {
                    continue;
                }
                return metadata.has("version") && this.mod.validVersions().test(Version.parse(metadata.get("version").getAsString()));
            } catch (IOException | VersionParsingException ignored) {
                return false;
            }
        }
        return false;
    }
}
