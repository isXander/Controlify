package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.api.contextual.Context;
import dev.isxander.controlify.api.contextual.TriggerEffectInstance;
import dev.isxander.controlify.contextual.ContextualDomainImpl;
import dev.isxander.controlify.contextual.ContextualState;
import dev.isxander.controlify.contextual.RuleEngine;
import dev.isxander.controlify.contextual.TriggerEffectRule;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.input.GamepadInputs;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.resources.Identifier;

import java.util.List;

public class TriggerEffectInstanceImpl<C extends Context> implements TriggerEffectInstance<C> {
	private final ContextualDomainImpl<C> domain;
	private final RuleEngine<Identifier, TriggerEffectRule> ruleEngine;

	private DualsenseTriggerEffect leftEffect = DualsenseTriggerEffect.Off.INSTANCE;
	private DualsenseTriggerEffect rightEffect = DualsenseTriggerEffect.Off.INSTANCE;

	public TriggerEffectInstanceImpl(
			ContextualDomainImpl<C> domain,
			RuleEngine<Identifier, TriggerEffectRule> ruleEngine
	) {
		this.domain = domain;
		this.ruleEngine = ruleEngine;
	}

	@Override
	public void update(C context) {
		boolean useTriggerEffects = context.controller().dualSense()
				.map(ds -> ds.settings().triggerEffects)
				.orElse(false);
		if (!useTriggerEffects) {
			this.leftEffect = DualsenseTriggerEffect.Off.INSTANCE;
			this.rightEffect = DualsenseTriggerEffect.Off.INSTANCE;
			return;
		}

		ContextualState state = this.domain.calculateState(context, this.ruleEngine.factDependencies());
		List<TriggerEffectRule> passedRules = this.ruleEngine.evaluate(
				state,
				rule -> getTrigger(context.controller(), rule) != Trigger.NEITHER
		);

		// there should only be one each
		this.leftEffect = passedRules.stream()
				.filter(rule -> getTrigger(context.controller(), rule) == Trigger.LEFT)
				.map(TriggerEffectRule::effect)
				.findAny()
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
		this.rightEffect = passedRules.stream()
				.filter(rule -> getTrigger(context.controller(), rule) == Trigger.RIGHT)
				.map(TriggerEffectRule::effect)
				.findAny()
				.orElse(DualsenseTriggerEffect.Off.INSTANCE);
	}

	@Override
	public DualsenseTriggerEffect getLeftTriggerEffect() {
		return this.leftEffect;
	}

	@Override
	public DualsenseTriggerEffect getRightTriggerEffect() {
		return this.rightEffect;
	}

	private Trigger getTrigger(ControllerEntity controller, TriggerEffectRule rule) {
		List<Identifier> relevantInputs = rule.binding().on(controller).boundInput().getRelevantInputs();

		if (relevantInputs.contains(GamepadInputs.LEFT_TRIGGER_AXIS)) {
			return Trigger.LEFT;
		} else if (relevantInputs.contains(GamepadInputs.RIGHT_TRIGGER_AXIS)) {
			return Trigger.RIGHT;
		}

		return Trigger.NEITHER;
	}

	private enum Trigger {
		LEFT, RIGHT, NEITHER
	}
}
