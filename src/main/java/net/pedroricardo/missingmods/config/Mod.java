package net.pedroricardo.missingmods.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public record Mod(String id, String link, VersionPredicate validVersions, ModEnvironment environment, Optional<Text> reason) {
    public static final Codec<Mod> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("id").forGetter(Mod::id), Codec.STRING.fieldOf("link").forGetter(Mod::link), Codec.of(Mod::encodeVersion, Mod::parseVersion).optionalFieldOf("valid_versions", VersionPredicateParser.getAny()).forGetter(Mod::validVersions), Codec.of(Mod::encodeEnvironment, Mod::parseEnvironment).optionalFieldOf("environment", ModEnvironment.UNIVERSAL).forGetter(Mod::environment), Codecs.TEXT.optionalFieldOf("reason").forGetter(Mod::reason)).apply(instance, Mod::new));

    private static <T> DataResult<T> encodeVersion(VersionPredicate version, DynamicOps<T> ops, T prefix) {
        return Codec.STRING.encode(version.toString(), ops, prefix);
    }

    private static <T> DataResult<T> encodeEnvironment(ModEnvironment environment, DynamicOps<T> ops, T prefix) {
        String str;
        if (environment == ModEnvironment.UNIVERSAL) {
            str = "*";
        } else if (environment == ModEnvironment.CLIENT) {
            str = "client";
        } else if (environment == ModEnvironment.SERVER) {
            str = "server";
        } else return DataResult.error(() -> "Unknown mod environment");
        return Codec.STRING.encode(str, ops, prefix);
    }

    private static <T> DataResult<Pair<ModEnvironment, T>> parseEnvironment(DynamicOps<T> ops, T input) {
        return switch (Codec.STRING.decode(ops, input).result().map(Pair::getFirst).orElse("*")) {
            case "*" -> DataResult.success(Pair.of(ModEnvironment.UNIVERSAL, input));
            case "server" -> DataResult.success(Pair.of(ModEnvironment.SERVER, input));
            case "client" -> DataResult.success(Pair.of(ModEnvironment.CLIENT, input));
            default -> DataResult.error(() -> "Unknown mod environment");
        };
    }

    private static <T> DataResult<Pair<VersionPredicate, T>> parseVersion(DynamicOps<T> ops, T input) {
        try {
            return DataResult.success(Pair.of(VersionPredicate.parse(Codec.STRING.decode(ops, input).result().map(Pair::getFirst).orElse("*")), input));
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Mod{" +
                "id='" + id + '\'' +
                ", link='" + link + '\'' +
                ", validVersions=" + validVersions +
                ", environment=" + environment +
                ", reason=" + reason.map(Text::getContent).orElse(null) +
                '}';
    }
}
