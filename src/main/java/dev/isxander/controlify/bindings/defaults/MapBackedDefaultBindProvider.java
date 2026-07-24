/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings.defaults;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.bindings.input.Input;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record MapBackedDefaultBindProvider(Map<Identifier, Input> map) implements DefaultBindProvider {
	public static final MapCodec<MapBackedDefaultBindProvider> MAP_CODEC = Codec.simpleMap(
			Identifier.CODEC, Input.CODEC,
			Keyable.forStrings(() -> ControlifyBindApi.get().getAllBindIds().map(Identifier::toString))
	).xmap(MapBackedDefaultBindProvider::new, MapBackedDefaultBindProvider::map);

	@Override
	public @Nullable Input getDefaultBind(Identifier bindId) {
		return map.get(bindId);
	}
}
