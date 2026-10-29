/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.impl.action.ActionCodecs;
import dev.isxander.controlify.impl.controller.dualsense.TriggerEffectCodecs;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.resources.Identifier;

/// @param action the action supplier associated with this action.
///                if the action is not bound to either trigger, it is ignored.
public record TriggerEffectRule(
		ActionAccessor action,
		ContextualPredicate predicate,
		DualsenseTriggerEffect effect
) implements Rule<Identifier> {
	public static final Codec<TriggerEffectRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ActionCodecs.ACCESSOR.fieldOf("for").forGetter(TriggerEffectRule::action),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(TriggerEffectRule::predicate),
			TriggerEffectCodecs.CODEC.fieldOf("then").forGetter(TriggerEffectRule::effect)
	).apply(instance, TriggerEffectRule::new));

	@Override
	public Identifier key() {
		return this.action.actionId();
	}
}
