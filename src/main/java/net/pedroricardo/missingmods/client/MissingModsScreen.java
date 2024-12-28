package net.pedroricardo.missingmods.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pedroricardo.missingmods.config.Mod;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MissingModsScreen extends Screen {
    private final List<Mod> required;
    private final List<Mod> optional;

    public MissingModsScreen(List<Mod> required, List<Mod> optional) {
        super(Text.translatable("missingmods.screen.title"));
        this.required = required;
        this.optional = optional;
    }

    @Override
    protected void init() {
        super.init();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignLeft();
        GridWidget.Adder adder = gridWidget.createAdder(1);
        if (!this.required.isEmpty()) {
            TextWidget requiredText = new TextWidget(Text.translatable("missingmods.screen.required").formatted(Formatting.YELLOW, Formatting.BOLD), this.textRenderer);
            requiredText.alignLeft();
            adder.add(requiredText);
        }
        for (Mod mod : this.required) {
            adder.add(new MissingModWidget(this.width - 10, mod, this.textRenderer, this));
        }
        if (!this.optional.isEmpty()) {
            TextWidget optionalText = new TextWidget(Text.translatable("missingmods.screen.optional").formatted(Formatting.YELLOW, Formatting.BOLD), this.textRenderer);
            optionalText.alignLeft();
            adder.add(optionalText);
        }
        for (Mod mod : this.optional) {
            adder.add(new MissingModWidget(this.width - 10, mod, this.textRenderer, this));
        }
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.0f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
