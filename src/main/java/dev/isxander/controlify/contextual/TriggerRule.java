package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.dualsense.TriggerEffectCodecs;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.resources.Identifier;

/// @param trigger identifier of the logical action that may be bound to a trigger.
///                for the in_game domain, the accepted values would be `use_item` and `swing_item`
public record TriggerRule(
		Identifier trigger,
		ContextualPredicate predicate,
		DualsenseTriggerEffect effect
) implements Rule<Identifier> {
	public static final Codec<TriggerRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("for").forGetter(TriggerRule::trigger),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(TriggerRule::predicate),
			TriggerEffectCodecs.CODEC.fieldOf("then").forGetter(TriggerRule::effect)
	).apply(instance, TriggerRule::new));

	@Override
	public Identifier key() {
		return this.trigger();
	}
}
