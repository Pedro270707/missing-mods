package net.pedroricardo.missingmods.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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
        this.missingModList = new MissingModsListWidget(this.client, this, this.required, this.optional);
        this.addSelectableChild(this.missingModList);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.missingModList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
