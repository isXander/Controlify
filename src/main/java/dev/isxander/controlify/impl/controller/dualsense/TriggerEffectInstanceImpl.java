/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.dualsense;

import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.TriggerEffectInstance;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.contextual.ContextualDomainImpl;
import dev.isxander.controlify.impl.contextual.ContextualState;
import dev.isxander.controlify.impl.contextual.RuleEngine;
import dev.isxander.controlify.impl.contextual.TriggerEffectRule;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.impl.input.mapping.GamepadInputs;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

public class TriggerEffectInstanceImpl<C extends Context> implements TriggerEffectInstance<C> {
	private final ContextualDomainImpl<C> domain;
	private final Supplier<RuleEngine<Identifier, TriggerEffectRule>> ruleEngineSupplier;

	private DualsenseTriggerEffect leftEffect = DualsenseTriggerEffect.Off.INSTANCE;
	private DualsenseTriggerEffect rightEffect = DualsenseTriggerEffect.Off.INSTANCE;

	public TriggerEffectInstanceImpl(
			ContextualDomainImpl<C> domain,
			Supplier<RuleEngine<Identifier, TriggerEffectRule>> ruleEngineSupplier
	) {
		this.domain = domain;
		this.ruleEngineSupplier = ruleEngineSupplier;
	}

	@Override
	public void update(C context) {
		var controller = (ControllerImpl) context.controller();

		RuleEngine<Identifier, TriggerEffectRule> ruleEngine = this.ruleEngineSupplier.get();
		boolean useTriggerEffects = controller.dualsense()
				.map(ds -> ds.settings().triggerEffects)
				.orElse(false);
		if (!useTriggerEffects) {
			this.leftEffect = DualsenseTriggerEffect.Off.INSTANCE;
			this.rightEffect = DualsenseTriggerEffect.Off.INSTANCE;
			return;
		}

		ContextualState state = this.domain.calculateState(context, ruleEngine.factDependencies());
		List<TriggerEffectRule> passedRules = ruleEngine.evaluate(
				state,
				rule -> getTrigger(controller, rule) != Trigger.NEITHER,
				"trigger_effect/" + this.domain.id()
		);

		// there should only be one each
		this.leftEffect = passedRules.stream()
				.filter(rule -> getTrigger(controller, rule) == Trigger.LEFT)
				.map(TriggerEffectRule::effect)
				.findAny()
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		this.rightEffect = passedRules.stream()
				.filter(rule -> getTrigger(controller, rule) == Trigger.RIGHT)
				.map(TriggerEffectRule::effect)
				.findAny()
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
	}

	@Override
	public @NonNull DualsenseTriggerEffect getLeftTriggerEffect() {
		return this.leftEffect;
	}

	@Override
	public @NonNull DualsenseTriggerEffect getRightTriggerEffect() {
		return this.rightEffect;
	}

	private Trigger getTrigger(ControllerImpl controller, TriggerEffectRule rule) {
		Action action = rule.action().onOrNull(controller);

		if (action == null) {
			return Trigger.NEITHER;
		}

		if (action.boundInput() instanceof LogicalInput.Single(Identifier physicalInput)) {
			if (GamepadInputs.LEFT_TRIGGER_AXIS.equals(physicalInput)) {
				return Trigger.LEFT;
			} else if (GamepadInputs.RIGHT_TRIGGER_AXIS.equals(physicalInput)) {
				return Trigger.RIGHT;
			}
		}

		return Trigger.NEITHER;
	}

	private enum Trigger {
		LEFT, RIGHT, NEITHER
	}
}
