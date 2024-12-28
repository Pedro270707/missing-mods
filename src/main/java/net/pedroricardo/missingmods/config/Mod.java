package net.pedroricardo.missingmods.config;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public record Mod(String id, String link, VersionPredicate validVersions, Optional<Text> reason) {
    public static final Codec<Mod> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("id").forGetter(Mod::id), Codec.STRING.fieldOf("link").forGetter(Mod::link), Codec.of(Mod::encodeVersion, Mod::parseVersion).optionalFieldOf("valid_versions", VersionPredicateParser.getAny()).forGetter(Mod::validVersions), Codecs.TEXT.optionalFieldOf("reason").forGetter(Mod::reason)).apply(instance, Mod::new));

    private static <T> DataResult<T> encodeVersion(VersionPredicate version, DynamicOps<T> ops, T prefix) {
        return Codec.STRING.encode(version.toString(), ops, prefix);
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
                ", reason=" + reason.map(Text::getContent).orElse(null) +
                '}';
    }
}
