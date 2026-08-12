package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.guide.ActionLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record GuideRule(
		InputBindingSupplier binding,
		ActionLocation location,
		ContextualPredicate predicate,
		Component text
) implements Rule<GuideRule.Key> {
	public static final Codec<GuideRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			InputBindingSupplier.CODEC.fieldOf("for").forGetter(GuideRule::binding),
			ActionLocation.CODEC.fieldOf("where").forGetter(GuideRule::location),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(GuideRule::predicate),
			ComponentSerialization.CODEC.fieldOf("then").forGetter(GuideRule::text)
	).apply(instance, GuideRule::new));

	@Override
	public Key key() {
		return new Key(this.binding().bindId(), this.location());
	}

	public record Key(
			Identifier binding,
			ActionLocation location
	) {}
}
