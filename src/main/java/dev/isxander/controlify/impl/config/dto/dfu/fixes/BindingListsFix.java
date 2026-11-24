/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.impl.config.dto.dfu.ControlifyTypeReferences;

import java.util.Map;
import java.util.stream.Stream;

/// Every action's bindings are a list; an empty list unbinds the action.
/// Earlier schemas stored a single bare input per action, with `{}` meaning unbound.
/// Both shapes are accepted, as {@link LogicalInputBindingsFix} wrote the bare form for schemas 11 to 14.
public final class BindingListsFix extends DataFix {
	public BindingListsFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: wrap bindings in lists",
				getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), root -> root.update("input", input ->
						input.update("bindings", bindings -> bindings.updateMapValues(entry ->
								entry.mapSecond(this::wrap)))))
		);
	}

	private <T> Dynamic<T> wrap(Dynamic<T> binding) {
		if (binding.asStreamOpt().result().isPresent()) {
			return binding;
		}
		if (binding.getMapValues().result().filter(Map::isEmpty).isPresent()) {
			return binding.createList(Stream.empty());
		}
		return binding.createList(Stream.of(binding));
	}
}
