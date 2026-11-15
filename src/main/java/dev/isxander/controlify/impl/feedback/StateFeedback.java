/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.impl.contextual.ContextualDomainImpl;
import dev.isxander.controlify.impl.contextual.ContextualDomains;
import dev.isxander.controlify.impl.contextual.ContextualState;
import dev.isxander.controlify.impl.contextual.FeedbackRule;
import dev.isxander.controlify.impl.contextual.RuleEngine;
import dev.isxander.controlify.impl.contextual.RuleSetManager;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/// Plays feedback from contextual state alone. Every game tick, each feedback rule without a
/// `for` whose predicate is true has its effect held, so a resource pack can add feedback for
/// any state the in-game domain describes without any code firing an event.
///
/// An effect whose envelopes end at zero is felt once, when the state becomes true. An effect
/// with a constant track is felt for as long as the state stays true.
public final class StateFeedback {
	public static void tick(Minecraft minecraft, ControllerImpl controller) {
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}

		ContextualDomainImpl<InGameContext> domain = ContextualDomains.INSTANCE.inGame();
		RuleEngine<Identifier, FeedbackRule> rules = RuleSetManager.FEEDBACK_RULES
				.getRuleEngine(minecraft.level, domain.id())
				.orElse(null);
		if (rules == null || !FeedbackSelection.hasStateDriven(rules)) {
			return;
		}

		ContextualState state = domain.calculateState(
				InGameContext.create(minecraft, controller),
				rules.factDependencies()
		);
		for (Identifier effect : FeedbackSelection.active(rules, state)) {
			controller.feedback().hold(effect);
		}
	}

	private StateFeedback() {
	}
}
