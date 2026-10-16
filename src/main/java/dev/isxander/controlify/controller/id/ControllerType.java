/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.id;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.driver.steamdeck.SteamDeckUtil;
import dev.isxander.controlify.utils.CUtil;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record ControllerType(
		Identifier namespace,
		@Nullable String friendlyName,
		@Nullable Identifier mappingId,
		boolean forceJoystick,
		boolean preventLoading
) {
	public static final ControllerType DEFAULT = new ControllerType(CUtil.rl("default"), null, null, false, false);

	public static final MapCodec<ControllerType> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("namespace", DEFAULT.namespace()).forGetter(ControllerType::namespace),
			CExtraCodecs.nullableField(Codec.STRING, "name").forGetter(ControllerType::friendlyName),
			CExtraCodecs.nullableField(Identifier.CODEC, "mapping").forGetter(ControllerType::mappingId),
			Codec.BOOL.optionalFieldOf("force_joystick", false).forGetter(ControllerType::forceJoystick),
			Codec.BOOL.optionalFieldOf("prevent_loading", false).forGetter(ControllerType::preventLoading)
	).apply(instance, ControllerType::new));

	public Identifier getIconSprite() {
		return namespace.withPrefix("controllers/");
	}

	public boolean isSteamDeck() {
		return namespace.equals(SteamDeckUtil.STEAM_DECK_NAMESPACE);
	}
}
