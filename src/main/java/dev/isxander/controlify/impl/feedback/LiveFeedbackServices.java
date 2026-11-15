/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.InputMode;
import dev.isxander.controlify.api.contextual.ContextualStateSink;
import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.contextual.ContextualDomainImpl;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.contextual.ContextualState;
import dev.isxander.controlify.impl.contextual.FeedbackRule;
import dev.isxander.controlify.impl.contextual.RuleEngine;
import dev.isxander.controlify.impl.contextual.RuleSetManager;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.controller.haptic.SteamHapticsComponent;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/// [FeedbackServices] backed by the running game, for one controller.
public final class LiveFeedbackServices implements FeedbackServices {
	private final ControllerImpl controller;

	public LiveFeedbackServices(ControllerImpl controller) {
		this.controller = controller;
	}

	/// Whether feedback should currently be silent: out of focus, paused (unless in the
	/// config screen, where vibration can be tested), or in keyboard and mouse mode.
	public static boolean shouldSilence(Minecraft minecraft, boolean outOfFocus, InputMode inputMode) {
		boolean isPaused = minecraft.isPaused() || MinecraftUtil.getScreen() instanceof PauseScreen;
		boolean isConfigScreen = MinecraftUtil.getScreen() instanceof YACLScreen;
		return outOfFocus || (isPaused && !isConfigScreen) || inputMode == InputMode.KEYBOARD_MOUSE;
	}

	@Override
	public float gain(Identifier eventId, Identifier effectId, FeedbackSource fallback) {
		FeedbackSourceManager sources = Controlify.instance().feedbackSourceManager();
		FeedbackSource source = sources.findAssigned(eventId)
				.or(() -> sources.findAssigned(effectId))
				.orElseGet(() -> sources.getSource(eventId, fallback));
		return this.controller.settings().rumble.strengthMultiplier(source);
	}

	@Override
	public boolean enabled() {
		return this.controller.settings().rumble.enabled;
	}

	@Override
	public boolean hd() {
		boolean available = this.controller.pcmHaptics().isPresent()
				|| this.controller.steamHaptics().map(SteamHapticsComponent::isWorking).orElse(false);
		return available && this.controller.settings().hdHaptic.enabled;
	}

	@Override
	public float falloff(Vec3 pos, float range) {
		var camera = Minecraft.getInstance().getCameraEntity();
		if (camera == null) {
			return -1f;
		}
		float distance = (float) Math.sqrt(camera.distanceToSqr(pos));
		float proximity = 1f - Mth.clamp(distance / range, 0f, 1f);
		return proximity * proximity;
	}

	@Override
	public @Nullable Identifier select(FeedbackEventImpl<?> event, @Nullable Consumer<ContextualStateSink> subject) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) {
			return null;
		}

		ContextualDomainImpl<InGameContext> domain = ContextualDomains.INSTANCE.inGame();
		RuleEngine<Identifier, FeedbackRule> rules = RuleSetManager.FEEDBACK_RULES
				.getRuleEngine(minecraft.level, domain.id())
				.orElse(null);
		if (rules == null) {
			return null;
		}

		ContextualState state = domain.calculateState(
				InGameContext.create(minecraft, this.controller),
				rules.factDependencies(),
				subject
		);
		return FeedbackSelection.select(rules, event.id(), state);
	}

	@Override
	public @Nullable FeedbackEffect<Object> findEffect(Identifier effectId, Identifier eventId) {
		return FeedbackRegistryImpl.INSTANCE.find(effectId, eventId);
	}
}
