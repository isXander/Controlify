package dev.isxander.controlify.api.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.utils.codec.SetCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A rule is made up of a set of conditions ({@link #when()} and {@link #forbid()})
 * that must be satisfied in order for a button guide to be shown.
 * <p>
 * These rules are loaded dynamically from resource packs or can be created programmatically
 * with {@link RuleBuilder}.
 *
 * @param binding the binding that this rule applies to - the glyph of which is shown in the guide
 * @param where the location of the guide (left or right)
 * @param when a set of conditions that all must be satisfied for the rule to apply
 * @param forbid a set of conditions where any must not be satisfied for the rule to apply
 * @param then the text component that is shown in the guide when the rule applies
 * @see #builder() for a builder to create dynamic rules
 */
public record Rule(
        InputBindingSupplier binding,
        ActionLocation where,
        Set<ResourceLocation> when,
        Set<ResourceLocation> forbid,
        Component then
) {
    public static final Codec<Rule> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    InputBindingSupplier.CODEC.fieldOf("for").forGetter(Rule::binding),
                    ActionLocation.CODEC.fieldOf("where").forGetter(Rule::where),
                    new SetCodec<>(ResourceLocation.CODEC).optionalFieldOf("when", Set.of()).forGetter(Rule::when),
                    new SetCodec<>(ResourceLocation.CODEC).optionalFieldOf("forbid", Set.of()).forGetter(Rule::forbid),
                    ComponentSerialization.CODEC.fieldOf("then").forGetter(Rule::then)
            ).apply(instance, Rule::new)
    );

    public static RuleBuilder builder() {
        return new RuleBuilder();
    }
}
