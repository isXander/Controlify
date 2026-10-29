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
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.impl.action.ActionCodecs;
import dev.isxander.controlify.impl.action.ActionSpecRegistryImpl;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.List;

public record MapBackedDefaultBindProvider(Map<Identifier, List<ActionBinding>> map) implements DefaultBindProvider {
	public static final MapCodec<MapBackedDefaultBindProvider> MAP_CODEC = Codec.simpleMap(
			Identifier.CODEC, ActionCodecs.BINDING.listOf(),
			Keyable.forStrings(() -> ActionSpecRegistryImpl.INSTANCE.getActionSpecs().stream().map(spec -> spec.id().toString()))
	).xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

	@Override
	public List<ActionBinding> getDefaultBindings(Identifier actionId) {
		return this.map.getOrDefault(actionId, List.of());
	}

	@Override
	public boolean hasDefaultBindings(Identifier actionId) {
		return this.map.containsKey(actionId);
	}
}
