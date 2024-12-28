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
import net.pedroricardo.missingmods.MissingMods;
import net.pedroricardo.missingmods.config.Mod;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Environment(EnvType.CLIENT)
public class MissingModsScreen extends Screen {
    private final List<Mod> required;
    private final List<Mod> optional;
    public DirectoryWatcher directoryWatcher;

    private MissingModsListWidget missingModList;
    private ButtonWidget quitGame;
    private long refreshTimeout;

    public MissingModsScreen(List<Mod> required, List<Mod> optional) {
        super(Text.translatable("missingmods.screen.title"));
        this.required = required;
        this.optional = optional;
        this.directoryWatcher = DirectoryWatcher.create(((FabricLoaderImpl) FabricLoader.getInstance()).getModsDirectory().toPath());
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
            this.quitGame = this.addDrawableChild(new ButtonWidget.Builder(Text.translatable("missingmods.screen.quit_game"), button -> {
                MinecraftClient.getInstance().close();
            }).width(bottomButtonWidth).build());
            this.quitGame.active = false;
            bottomButtons.add(this.quitGame);
        }

        topButtons.refreshPositions();
        bottomButtons.refreshPositions();
        SimplePositioningWidget.setPos(topButtons, 0, this.height - 74, this.width, 64);
        SimplePositioningWidget.setPos(bottomButtons, 0, this.height - 54, this.width, 64);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.missingModList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (this.directoryWatcher != null) {
            try {
                if (this.directoryWatcher.pollForChange()) {
                    this.refreshTimeout = 20L;
                }
            } catch (IOException iOException) {
                MissingMods.LOGGER.warn("Failed to poll for mod directory changes, stopping");
                this.closeDirectoryWatcher();
            }
        }
        if (this.refreshTimeout > 0L && --this.refreshTimeout == 0L) {
            this.quitGame.active = true;
            for (MissingModWidget widget : this.missingModList.getOptionalModEntries().stream().map(MissingModsListWidget.ModEntry::getWidget).toList()) {
                widget.update();
            }
            for (MissingModWidget widget : this.missingModList.getRequiredModEntries().stream().map(MissingModsListWidget.ModEntry::getWidget).toList()) {
                widget.update();
                if (!widget.isModPresent()) {
                    this.quitGame.active = false;
                }
            }
        }
    }

    private void closeDirectoryWatcher() {
        if (this.directoryWatcher != null) {
            try {
                this.directoryWatcher.close();
                this.directoryWatcher = null;
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DirectoryWatcher implements AutoCloseable {
        private final WatchService watchService;
        private final Path path;

        public DirectoryWatcher(Path path) throws IOException {
            this.path = path;
            this.watchService = path.getFileSystem().newWatchService();
            try {
                this.watchDirectory(path);
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)){
                    for (Path path2 : directoryStream) {
                        if (!Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) continue;
                        this.watchDirectory(path2);
                    }
                }
            } catch (Exception e) {
                this.watchService.close();
                throw e;
            }
        }

        @Nullable
        public static DirectoryWatcher create(Path path) {
            try {
                return new DirectoryWatcher(path);
            } catch (IOException e) {
                MissingMods.LOGGER.warn("Failed to initialize mod directory {} monitoring", path, e);
                return null;
            }
        }

        private void watchDirectory(Path path) throws IOException {
            path.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChange() throws IOException {
            WatchKey watchKey;
            boolean bl = false;
            while ((watchKey = this.watchService.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    Path path;
                    bl = true;
                    if (watchKey.watchable() != this.path || watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE || !Files.isDirectory(path = this.path.resolve((Path)watchEvent.context()), LinkOption.NOFOLLOW_LINKS)) continue;
                    this.watchDirectory(path);
                }
                watchKey.reset();
            }
            return bl;
        }

        @Override
        public void close() throws IOException {
            this.watchService.close();
        }
    }
}
