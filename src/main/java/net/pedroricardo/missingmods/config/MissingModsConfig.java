package net.pedroricardo.missingmods.config;

import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MissingModsConfig extends SimpleConfig {
    public final Supplier<List<Mod>> required;
    public final Supplier<List<Mod>> optional;

    public MissingModsConfig() {
        super("missing-mods");
        this.required = register("required", Mod.CODEC.listOf(), List.of(new Mod("deeperdarker", "https://modrinth.com", VersionPredicateParser.getAny(), Optional.empty())));
        this.optional = register("optional", Mod.CODEC.listOf(), List.of(new Mod("deeperdarker", "https://modrinth.com", VersionPredicateParser.getAny(), Optional.empty())));
    }
}
