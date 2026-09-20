/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record RuleSet<K, R extends Rule<K>>(
		boolean replace,
		List<R> rules
) {
	public static <K, R extends Rule<K>> Codec<RuleSet<K, R>> createCodec(Codec<R> ruleCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(RuleSet::replace),
				ruleCodec.listOf().fieldOf("rules").forGetter(RuleSet::rules)
		).apply(instance, RuleSet::new));
	}
}
