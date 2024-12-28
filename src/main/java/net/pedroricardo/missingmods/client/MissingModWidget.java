package net.pedroricardo.missingmods.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.pedroricardo.missingmods.config.Mod;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MissingModWidget extends ClickableWidget {
    private static final Text DOWNLOAD_TEXT = Text.translatable("missingmods.mod.download").formatted(Formatting.BLUE, Formatting.UNDERLINE);

    private final Mod mod;
    private final TextRenderer textRenderer;
    private final Screen parent;
    private boolean modPresent = false;

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
        Text message = this.modPresent ? this.getMessage().copy().formatted(Formatting.STRIKETHROUGH) : this.getMessage();
        int color = this.modPresent ? 0x99FFFFFF : 0xFFFFFFFF;
        this.drawLeftAlignedScrollableText(context, this.textRenderer, message, 2, color);
        if (this.modPresent) {
            context.drawTexture(new Identifier("textures/gui/checkmark.png"), this.getX() + 9 + this.textRenderer.getWidth(message), this.getY() + 5, 0.0f, 0.0f, 9, 8, 9, 8);
        }
        if (!this.modPresent && this.isSelected()) {
            context.getMatrices().translate(0.0f, 0.0f, 1.0f);
            context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xDD000000);
            this.drawScrollableText(context, this.textRenderer, DOWNLOAD_TEXT, 2, 0xFFFFFFFF);
            context.getMatrices().translate(0.0f, 0.0f, -1.0f);
        }
        if (this.isHovered() && this.mod.reason().isPresent()) {
            context.drawTooltip(this.textRenderer, Tooltip.of(this.mod.reason().get()).getLines(MinecraftClient.getInstance()), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
        }
    }

    protected void drawScrollableText(DrawContext context, TextRenderer textRenderer, Text text, int xMargin, int color) {
        int i = this.getX() + xMargin;
        int j = this.getX() + this.getWidth() - xMargin;
        drawScrollableText(context, textRenderer, text, i, this.getY(), j, this.getY() + this.getHeight(), color);
    }

    protected void drawLeftAlignedScrollableText(DrawContext context, TextRenderer textRenderer, Text text, int xMargin, int color) {
        int i = this.getX() + xMargin;
        int j = this.getX() + this.getWidth() - xMargin;
        drawLeftAlignedScrollableText(context, textRenderer, text, i, this.getY(), j, this.getY() + this.getHeight(), color);
    }

    protected static void drawLeftAlignedScrollableText(DrawContext context, TextRenderer textRenderer, Text text, int left, int top, int right, int bottom, int color) {
        int width = textRenderer.getWidth(text);
        int textTop = (top + bottom - textRenderer.fontHeight) / 2 + 1;
        int widgetWidth = right - left;
        if (width > widgetWidth) {
            int overflow = width - widgetWidth;
            double time = (double)Util.getMeasuringTimeMs() / 1000.0;
            double speed = Math.max((double)overflow * 0.5, 3.0);
            double displacement = Math.sin(1.5707963267948966 * Math.cos(Math.PI * 2 * time / speed)) / 2.0 + 0.5;
            double lerped = MathHelper.lerp(displacement, 0.0, overflow);
            context.enableScissor(left, top, right, bottom);
            context.drawTextWithShadow(textRenderer, text, left - (int)lerped, textTop, color);
            context.disableScissor();
        } else {
            context.drawText(textRenderer, text, left, textTop, color, true);
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.modPresent) {
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

    public void update() {
        this.modPresent = this.checkForMod();
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

    public boolean isModPresent() {
        return this.modPresent;
    }
}
