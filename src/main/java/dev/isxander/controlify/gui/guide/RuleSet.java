package dev.isxander.controlify.gui.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.guide.Rule;

import java.util.List;

public record RuleSet(
        boolean replace,
        List<Rule> rules
) {
    public static final Codec<RuleSet> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(RuleSet::replace),
                    Rule.CODEC.listOf().fieldOf("rules").forGetter(RuleSet::rules)
            ).apply(instance, RuleSet::new)
    );
}
