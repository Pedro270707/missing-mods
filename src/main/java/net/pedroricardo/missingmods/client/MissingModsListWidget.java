package net.pedroricardo.missingmods.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pedroricardo.missingmods.config.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MissingModsListWidget extends ElementListWidget<MissingModsListWidget.Entry> {
    private final List<ModEntry> required;
    private final List<ModEntry> optional;

    public MissingModsListWidget(MinecraftClient client, MissingModsScreen parent, List<Mod> required, List<Mod> optional) {
        super(client, parent.width, parent.height, 32, parent.height - 64, 17);
        List<ModEntry> requiredEntries = new ArrayList<>();
        List<ModEntry> optionalEntries = new ArrayList<>();
        if (!required.isEmpty()) {
            this.addEntry(new CategoryEntry(Text.translatable("missingmods.screen.required")));
        }
        for (Mod mod : required) {
            ModEntry entry = new ModEntry(new MissingModWidget(232, mod, client.textRenderer, parent));
            this.addEntry(entry);
            requiredEntries.add(entry);
        }
        if (!optional.isEmpty()) {
            this.addEntry(new CategoryEntry(Text.translatable("missingmods.screen.optional")));
        }
        for (Mod mod : optional) {
            ModEntry entry = new ModEntry(new MissingModWidget(232, mod, client.textRenderer, parent));
            this.addEntry(entry);
            optionalEntries.add(entry);
        }
        this.required = Collections.unmodifiableList(requiredEntries);
        this.optional = Collections.unmodifiableList(optionalEntries);
    }

    public List<ModEntry> getRequiredModEntries() {
        return this.required;
    }

    public List<ModEntry> getOptionalModEntries() {
        return this.optional;
    }

    @Environment(EnvType.CLIENT)
    public class CategoryEntry extends Entry {
        private final Text text;
        private final int textWidth;

        public CategoryEntry(Text text) {
            this.text = text;
            this.textWidth = MissingModsListWidget.this.client.textRenderer.getWidth(this.text);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(MissingModsListWidget.this.client.textRenderer, this.text.copy().formatted(Formatting.YELLOW, Formatting.BOLD), MissingModsListWidget.this.client.currentScreen.width / 2 - this.textWidth / 2, y + entryHeight - MissingModsListWidget.this.client.textRenderer.fontHeight + 1, 0xFFFFFFFF, true);
        }
    }

    @Environment(EnvType.CLIENT)
    public class ModEntry extends Entry {
        private final MissingModWidget widget;

        public ModEntry(MissingModWidget widget) {
            this.widget = widget;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(this.widget);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(this.widget);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.widget.setX(x);
            this.widget.setY(y);
            this.widget.render(context, mouseX, mouseY, tickDelta);
        }

        public MissingModWidget getWidget() {
            return this.widget;
        }
    }

    @Environment(value= EnvType.CLIENT)
    public static abstract class Entry extends ElementListWidget.Entry<Entry> {
    }
}
