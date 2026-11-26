/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.InputMode;
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
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// [FeedbackServices] backed by the running game, for one controller.
public final class LiveFeedbackServices implements FeedbackServices {
	private static final FeedbackGroup GUI_GROUP = new FeedbackGroup(CUtil.rl("gui"));

	private final ControllerImpl controller;

	/// The in-game state rules are matched against. It is calculated at most once per game
	/// tick, by the first event in that tick that has rules, and shared by the rest.
	private @Nullable ContextualState ruleState;
	private @Nullable RuleEngine<Identifier, FeedbackRule> ruleStateRules;
	private long ruleStateTick;

	public LiveFeedbackServices(ControllerImpl controller) {
		this.controller = controller;
	}

	/// Whether all feedback should currently be silent: out of focus, or in keyboard and mouse mode.
	public static boolean shouldSilence(boolean outOfFocus, InputMode inputMode) {
		return outOfFocus || inputMode == InputMode.KEYBOARD_MOUSE;
	}

	/// Whether the feedback of the game itself should currently be silent: paused, except in the
	/// config screen, where vibration can be tested. Feedback for menus is still felt.
	public static boolean shouldSilenceGame(Minecraft minecraft) {
		boolean isPaused = minecraft.isPaused() || MinecraftUtil.getScreen() instanceof PauseScreen;
		boolean isConfigScreen = MinecraftUtil.getScreen() instanceof YACLScreen;
		return isPaused && !isConfigScreen;
	}

	@Override
	public boolean feltWhilePaused(Identifier effectId, FeedbackGroup fallback) {
		return Controlify.instance().feedbackGroupManager().groups()
				.membership(effectId, fallback).groups().contains(GUI_GROUP);
	}

	@Override
	public Mixing mixing(Identifier effectId, FeedbackGroup fallback) {
		FeedbackGroups.Membership membership = Controlify.instance().feedbackGroupManager().groups()
				.membership(effectId, fallback);
		return new Mixing(this.controller.settings().rumble.strengthMultiplier(membership.groups()), membership.rank());
	}

	@Override
	public Mixing mixing(List<FeedbackGroup> groups) {
		FeedbackGroups known = Controlify.instance().feedbackGroupManager().groups();
		int rank = groups.stream().mapToInt(known::rank).max().orElse(0);
		return new Mixing(this.controller.settings().rumble.strengthMultiplier(groups), rank);
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
		float distance = (float) camera.position().distanceTo(pos);
		return Mth.square(Mth.clampedMap(distance, 0f, range, 1f, 0f));
	}

	@Override
	public List<Identifier> select(FeedbackEventImpl<?> event) {
		Minecraft minecraft = Minecraft.getInstance();
		ContextualDomainImpl<InGameContext> domain = ContextualDomains.INSTANCE.inGame();
		RuleEngine<Identifier, FeedbackRule> rules = minecraft.player == null || minecraft.level == null
				? null
				: RuleSetManager.FEEDBACK_RULES.getRuleEngine(minecraft.level, domain.id()).orElse(null);
		if (rules == null) {
			// do not keep the state of a world that has been left
			this.ruleState = null;
			this.ruleStateRules = null;
			return List.of(event.id());
		}
		// most events have no rules, so they never cause the state to be calculated
		if (!FeedbackSelection.hasRulesFor(rules, event.id())) {
			return List.of(event.id());
		}

		long tick = Controlify.instance().fixedUpdateCount();
		if (this.ruleState == null || this.ruleStateRules != rules || this.ruleStateTick != tick) {
			this.ruleState = domain.calculateState(
					InGameContext.create(minecraft, this.controller),
					rules.factDependencies()
			);
			this.ruleStateRules = rules;
			this.ruleStateTick = tick;
		}
		return FeedbackSelection.select(rules, event.id(), this.ruleState);
	}

	@Override
	public @Nullable FeedbackEffect<Object> findEffect(Identifier effectId, Identifier eventId) {
		return FeedbackRegistryImpl.INSTANCE.find(effectId, eventId);
	}
}
