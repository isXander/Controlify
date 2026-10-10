package dev.isxander.controlify.input.action.activator;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;
import net.minecraft.util.StringRepresentable;

/// An immutable structure typically containing the configuration
/// for an {@link Activator} type, allowing you to create instances
/// of the activator using the configuration and a given bound logical input.
public interface ActivatorConfig<T extends Activator> {
	T create(LogicalInput boundInput);

	ActivatorConfigType<T, ?> type();

	Codec<ActivatorConfig<?>> CODEC = StringRepresentable.fromValues(() -> ActivatorConfigType.TYPES)
			.dispatch("type", ActivatorConfig::type, ActivatorConfigType::mapCodec);
}
