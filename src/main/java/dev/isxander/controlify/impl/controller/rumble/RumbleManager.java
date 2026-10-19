/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.Controlify;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class RumbleManager {
	private final RumbleComponentImpl controller;
	private final List<PlayingEffect> effectQueue = new ArrayList<>();

	private boolean silent, wasSilent;

	public RumbleManager(RumbleComponentImpl controller) {
		this.controller = controller;
	}

	public RumbleEffectInstance play(Identifier effectId, RumbleEffect effect) {
		return play(effectId, RumbleSource.MASTER, effect);
	}

	/// Internal entry point for server-specified fallback sources.
	public RumbleEffectInstance play(Identifier effectId, RumbleSource source, RumbleEffect effect) {
		var instance = new RumbleEffectInstanceImpl(effectId, effect);
		this.effectQueue.add(new PlayingEffect(source, instance));
		return instance;
	}

	public void tick() {
		this.effectQueue.removeIf(e -> e.effect().hasFinished());
		this.effectQueue.forEach(e -> e.effect().tick());

		if (effectQueue.isEmpty()) {
			clearRumble();
			return;
		}

		float strong = 0f, weak = 0f;
		RumbleSourceManager sourceManager = Controlify.instance().rumbleSourceManager();
		for (PlayingEffect effect : effectQueue) {
			RumbleSource source = sourceManager.getSource(effect.effect().effectId(), effect.source());
			RumbleState effectState = controller.settings().applyRumbleStrength(effect.effect().currentState(), source);
			strong = Math.max(strong, effectState.strong());
			weak = Math.max(weak, effectState.weak());
		}
		RumbleState state = new RumbleState(strong, weak);

		if (state.isZero()) {
			clearRumble();
			return;
		}

		if (silent || !controller.settings().enabled) {
			clearRumble();
		} else {
			controller.queueRumble(state);
			wasSilent = false;
		}
	}

	private void clearRumble() {
		if (wasSilent)
			return;

		controller.queueRumble(RumbleState.NONE);
		wasSilent = true;
	}

	public void clearEffects() {
		for (PlayingEffect instance : effectQueue) {
			instance.effect().stop();
		}
		effectQueue.clear();
		clearRumble();
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public boolean isPlaying() {
		return !effectQueue.isEmpty();
	}

	private record PlayingEffect(RumbleSource source, RumbleEffectInstanceImpl effect) {
	}
}
