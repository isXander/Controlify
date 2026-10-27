/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.isxander.controlify.api.action.ActionBinding;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Optional;

/// An immutable structure containing the configuration for an {@link Activator} tree,
/// allowing you to create instances of it for an action's bindings.
public interface ActivatorConfig<T extends Activator> {
	/// Creates the activator tree. Each leaf gets one instance per binding; everything above the
	/// leaves exists once and sees the bindings merged, as if there were a single binding.
	T create(List<ActionBinding> bindings);

	ActivatorConfigType<T, ?> type();

	/// Every leaf in this tree, depth first.
	List<LeafConfig> leaves();

	/// The single leaf whose gesture a binding may override, if any.
	default Optional<LeafConfig> editableLeaf() {
		return this.leaves().stream().filter(LeafConfig::editable).findFirst();
	}

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
				.withAlternative(self.listOf(), CompoundActivator.Config::new)
				.validate(config -> config.leaves().stream().filter(LeafConfig::editable).count() > 1
						? DataResult.error(() -> "An activator may have at most one editable leaf")
						: DataResult.success(config));
	});
}
