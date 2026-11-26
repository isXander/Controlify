/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.sound;

import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public final class ControlifyClientSounds {
	public static final Supplier<SoundEvent> SCREEN_FOCUS_CHANGE = register("controlify.ui.focus");

	private static Supplier<SoundEvent> register(String id) {
		Identifier location = CUtil.rl(id);

		// don't actually register the event, so it isn't synced with the server
		SoundEvent sound = SoundEvent.createVariableRangeEvent(location);
		return () -> sound;
	}

	/// A UI sound at the default UI volume. 26.4 dropped the `forUI(sound, pitch)` overloads.
	public static SimpleSoundInstance forUI(SoundEvent sound, float pitch) {
		return forUI(Holder.direct(sound), pitch);
	}

	public static SimpleSoundInstance forUI(Holder<SoundEvent> sound, float pitch) {
		//? if >=26.4 {
		return SimpleSoundInstance.forUI(sound, pitch, 0.25f);
		//?} else {
		//return SimpleSoundInstance.forUI(sound, pitch);
		//?}
	}

	public static void init() {
		// calling init now calls <clinit> which is where the sounds are registered
	}
}
