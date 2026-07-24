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

import java.util.stream.Stream;

public final class AnalogueMovementWhitelistFix extends DataFix {
	public AnalogueMovementWhitelistFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

		return fixTypeEverywhereTyped(
				"Controlify: replace keyboard movement whitelist with analogue movement whitelist",
				type,
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		Dynamic<T> global = root.get("global").orElseEmptyMap()
				.remove("keyboard_movement_whitelist")
				.set("analogue_movement_whitelist", root.createList(Stream.empty()))
				.set("seen_servers", root.createList(Stream.empty()));

		return root.set("global", global);
	}
}
