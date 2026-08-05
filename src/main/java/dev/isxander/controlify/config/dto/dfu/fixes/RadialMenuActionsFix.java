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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class RadialMenuActionsFix extends DataFix {
	private static final List<String> DEBUG_ACTIONS = List.of(
			"controlify:toggle_debug_menu",
			"controlify:toggle_debug_menu_fps",
			"controlify:toggle_debug_menu_net",
			"controlify:toggle_debug_menu_prof",
			"controlify:debug_reload_chunks",
			"controlify:debug_toggle_chunk_borders",
			"controlify:debug_toggle_advanced_tooltips",
			"controlify:debug_toggle_entity_hitboxes",
			"controlify:debug_reload_resource_packs",
			"controlify:debug_clear_chat",
			"controlify:debug_start_stop_profiling"
	);

	public RadialMenuActionsFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: expand and deduplicate radial menu actions",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		Dynamic<T> input = root.get("input").orElseEmptyMap();
		Dynamic<T> radialMenu = input.get("radial_menu").orElseEmptyMap();
		var uniqueActions = new LinkedHashSet<String>();
		radialMenu.get("actions").asStream()
				.map(Dynamic::asString)
				.map(result -> result.result().orElse(null))
				.filter(Objects::nonNull)
				.forEach(uniqueActions::add);
		uniqueActions.addAll(DEBUG_ACTIONS);
		radialMenu = radialMenu.set("actions", root.createList(uniqueActions.stream().map(root::createString)));

		return root.set("input", input.set("radial_menu", radialMenu));
	}
}
