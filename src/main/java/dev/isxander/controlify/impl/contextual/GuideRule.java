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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public record GuideRule(
		ActionAccessor action,
		GuideLocation location,
		ContextualPredicate predicate,
		Component text
) implements Rule<GuideRule.Key> {
	public static final Codec<GuideRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ActionAccessor.CODEC.fieldOf("for").forGetter(GuideRule::action),
			GuideLocation.CODEC.fieldOf("where").forGetter(GuideRule::location),
			ContextualPredicate.CODEC.fieldOf("if").forGetter(GuideRule::predicate),
			ComponentSerialization.CODEC.fieldOf("then").forGetter(GuideRule::text)
	).apply(instance, GuideRule::new));

	@Override
	public Key key() {
		return new Key(this.action().actionId(), this.location());
	}

	public record Key(
			Identifier action,
			GuideLocation location
	) {}
}
