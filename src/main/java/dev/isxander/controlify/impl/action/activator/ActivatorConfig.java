/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.isxander.controlify.api.input.LogicalInput;
import net.minecraft.util.StringRepresentable;

/// An immutable structure typically containing the configuration
/// for an {@link Activator} type, allowing you to create instances
/// of the activator using the configuration and a given bound logical input.
public interface ActivatorConfig<T extends Activator> {
	T create(LogicalInput boundInput);

	ActivatorConfigType<T, ?> type();

	Codec<ActivatorConfig<?>> CODEC = Codec.recursive("ActivatorConfig", self -> {
		Codec<ActivatorConfigType<?, ?>> typeCodec = StringRepresentable.fromValues(() -> ActivatorConfigType.TYPES);
		Codec<ActivatorConfig<?>> fullCodec = typeCodec.dispatch("type", ActivatorConfig::type, ActivatorConfigType::mapCodec);
		Codec<ActivatorConfig<?>> shorthandCodec = typeCodec.flatXmap(
				type -> type.unitConfig()
						.<DataResult<ActivatorConfig<?>>>map(DataResult::success)
						.orElseGet(() -> DataResult.error(() -> "Activator type '" + type.id() + "' requires an object configuration")),
				config -> DataResult.success(config.type())
		);

		// Alternatives are accepted when decoding; encoding always uses the full object form.
		return fullCodec.withAlternative(shorthandCodec)
				.withAlternative(self.listOf(), CompoundActivator.Config::new);
	});
}
