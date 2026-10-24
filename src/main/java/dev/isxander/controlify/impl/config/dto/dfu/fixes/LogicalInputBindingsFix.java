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

import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class LogicalInputBindingsFix extends DataFix {
	public LogicalInputBindingsFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: migrate bindings to logical inputs",
				getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), root -> root.update("input", input ->
						input.update("bindings", bindings -> bindings.updateMapValues(entry ->
								entry.mapSecond(this::rewriteInput)))))
		);
	}

	private <T> Dynamic<T> rewriteInput(Dynamic<T> input) {
		for (String field : new String[]{"button", "axis"}) {
			var identifier = input.get(field).asString().result();
			if (identifier.isPresent()) {
				return input.createString(identifier.get());
			}
		}
		var hat = input.get("hat").asString().result();
		var direction = input.get("target_state").asString().result();
		if (hat.isPresent() && direction.isPresent()) {
			String name = direction.get().toLowerCase(Locale.ROOT);
			var parts = Arrays.asList(name.split("_"));
			var buttons = List.of("up", "right", "down", "left").stream().filter(parts::contains).toList();
			if (buttons.size() == 2) {
				// Diagonals use the same cardinal button pair emitted by SDLToPhysicalStage.
				return input.createMap(Map.of(
						input.createString("modifiers"), input.createList(Stream.of(input.createString(hat.get() + "/" + buttons.getFirst()))),
						input.createString("trigger"), input.createString(hat.get() + "/" + buttons.getLast())
				));
			}
			return name.equals("centered") ? input.emptyMap()
					: input.createString(hat.get() + "/" + name);
		}
		if (input.get("type").asString().result().filter("empty"::equals).isPresent()) {
			return input.emptyMap();
		}
		// Already-converted strings, chords and explicit unbindings retain their meaning.
		return input;
	}
}
