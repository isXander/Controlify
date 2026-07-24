/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;

public final class HorizontalLookInvertFix extends DataFix {
	public HorizontalLookInvertFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

		return fixTypeEverywhereTyped(
				"Controlify: add horizontal look inversion",
				type,
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		return root.update("profiles", profiles -> profiles.createList(
				profiles.asStream().map(profile -> profile.update(
						"input",
						input -> input.update(
								"sensitivity",
								sensitivity -> sensitivity.set(
										"horizontal_invert",
										sensitivity.createBoolean(false)
								)
						)
				))
		));
	}
}
