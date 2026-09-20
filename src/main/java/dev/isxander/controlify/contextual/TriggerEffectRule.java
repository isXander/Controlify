/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.controller.dualsense.TriggerEffectCodecs;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import net.minecraft.resources.Identifier;

/// @param binding the binding supplier associated with this action.
///                if the binding is not bound to either trigger, it is ignored.
public record TriggerEffectRule(
		InputBindingSupplier binding,
		ContextualPredicate predicate,
		DualsenseTriggerEffect effect
) implements Rule<Identifier> {
	public static final Codec<TriggerEffectRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			InputBindingSupplier.CODEC.fieldOf("for").forGetter(TriggerEffectRule::binding),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(TriggerEffectRule::predicate),
			TriggerEffectCodecs.CODEC.fieldOf("then").forGetter(TriggerEffectRule::effect)
	).apply(instance, TriggerEffectRule::new));

	@Override
	public Identifier key() {
		return this.binding.bindId();
	}
}
