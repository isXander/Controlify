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

import java.util.LinkedHashMap;
import java.util.Map;

public final class GeneratedBindingIdsFix extends DataFix {
	private static final String OLD_NAMESPACE = "fabric-key-binding-api-v1:";
	private static final String NEW_NAMESPACE = "controlify_modded:";

	public GeneratedBindingIdsFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: migrate generated binding IDs",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		return root.update("input", input -> input
				.update("bindings", this::rewriteBindings)
				.update("radial_menu", radialMenu -> radialMenu.update("actions", this::rewriteRadialActions))
		);
	}

	private <T> Dynamic<T> rewriteBindings(Dynamic<T> bindings) {
		return bindings.getMapValues().result()
				.map(entries -> rewriteBindings(bindings, entries))
				.orElse(bindings);
	}

	private <T> Dynamic<T> rewriteBindings(Dynamic<T> bindings, Map<Dynamic<T>, Dynamic<T>> entries) {
		var rewritten = new LinkedHashMap<Dynamic<T>, Dynamic<T>>();

		// Keep explicitly configured new IDs if both forms happen to exist.
		entries.forEach((key, value) -> {
			if (!isLegacyId(key)) {
				rewritten.put(key, value);
			}
		});
		entries.forEach((key, value) -> {
			if (isLegacyId(key)) {
				rewritten.putIfAbsent(rewriteId(key), value);
			}
		});

		return bindings.createMap(rewritten);
	}

	private <T> Dynamic<T> rewriteRadialActions(Dynamic<T> actions) {
		return actions.asStreamOpt().result()
				.map(stream -> actions.createList(stream.map(this::rewriteId)))
				.orElse(actions);
	}

	private boolean isLegacyId(Dynamic<?> value) {
		return value.asString().result()
				.map(id -> id.startsWith(OLD_NAMESPACE))
				.orElse(false);
	}

	private <T> Dynamic<T> rewriteId(Dynamic<T> value) {
		return value.asString().result()
				.filter(id -> id.startsWith(OLD_NAMESPACE))
				.map(id -> value.createString(NEW_NAMESPACE + id.substring(OLD_NAMESPACE.length())))
				.orElse(value);
	}
}
