package net.pedroricardo.missingmods.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AxisGridWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.pedroricardo.missingmods.config.Mod;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MissingModsScreen extends Screen {
    private final List<Mod> required;
    private final List<Mod> optional;

    private MissingModsListWidget missingModList;

    public MissingModsScreen(List<Mod> required, List<Mod> optional) {
        super(Text.translatable("missingmods.screen.title"));
        this.required = required;
        this.optional = optional;
    }

    @Override
    protected void init() {
        super.init();
        this.missingModList = this.addSelectableChild(new MissingModsListWidget(this.client, this, this.required, this.optional));

        GridWidget gridWidget = new GridWidget();
        GridWidget.Adder adder = gridWidget.createAdder(1);

        AxisGridWidget topButtons = adder.add(new AxisGridWidget(300, 20, AxisGridWidget.DisplayAxis.HORIZONTAL));
        ButtonWidget openAll = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.open_all"), button -> {
            for (Mod mod : this.required) {
                Util.getOperatingSystem().open(mod.link());
            }
            for (Mod mod : this.optional) {
                Util.getOperatingSystem().open(mod.link());
            }
        }).width(150).build());
        ButtonWidget openRequired = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.open_required"), button -> {
            for (Mod mod : this.required) {
                Util.getOperatingSystem().open(mod.link());
            }
        }).width(150).build());

        int bottomButtonWidth = this.required.isEmpty() ? 150 : 100;
        AxisGridWidget bottomButtons = new AxisGridWidget(300, 20, AxisGridWidget.DisplayAxis.HORIZONTAL);
        ButtonWidget modsDir = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.open_mod_directory"), button -> {
            Util.getOperatingSystem().open(((FabricLoaderImpl) FabricLoader.getInstance()).getModsDirectory());
        }).width(bottomButtonWidth).build());
        ButtonWidget ignoreAndPlay = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.ignore_and_play"), button -> {
            this.client.setScreen(null);
        }).width(bottomButtonWidth).build());

        topButtons.add(openAll);
        topButtons.add(openRequired);
        bottomButtons.add(modsDir);
        bottomButtons.add(ignoreAndPlay);

        if (!this.required.isEmpty()) {
            ignoreAndPlay.active = false;
            ButtonWidget closeGame = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.quit_game"), button -> {
                MinecraftClient.getInstance().close();
            }).width(bottomButtonWidth).build());
            closeGame.active = false;
            bottomButtons.add(closeGame);
        }

        topButtons.refreshPositions();
        bottomButtons.refreshPositions();
        SimplePositioningWidget.setPos(topButtons, 0, this.height - 74, this.width, 64);
        SimplePositioningWidget.setPos(bottomButtons, 0, this.height - 54, this.width, 64);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.missingModList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
