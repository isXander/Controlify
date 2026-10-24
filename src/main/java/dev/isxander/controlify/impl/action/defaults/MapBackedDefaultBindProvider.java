/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.defaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public record MapBackedDefaultBindProvider(Map<Identifier, Optional<LogicalInput>> map) implements DefaultBindProvider {
	public static final MapCodec<MapBackedDefaultBindProvider> MAP_CODEC = Codec.simpleMap(
			Identifier.CODEC, LogicalInput.OPTIONAL_CODEC,
			Keyable.forStrings(() -> ActionSpecRegistryImpl.INSTANCE.getActionSpecs().stream().map(spec -> spec.id().toString()))
	).xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

	@Override
	public @Nullable LogicalInput getDefaultInput(Identifier actionId) {
		return this.map.getOrDefault(actionId, Optional.empty()).orElse(null);
	}

	@Override
	public boolean hasDefaultInput(Identifier actionId) {
		return this.map.containsKey(actionId);
	}
}
