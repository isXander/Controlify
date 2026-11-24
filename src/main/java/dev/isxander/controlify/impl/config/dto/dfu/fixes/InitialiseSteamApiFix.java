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
import dev.isxander.controlify.impl.config.settings.GlobalSettings;

public final class InitialiseSteamApiFix extends DataFix {
	private final GlobalSettings globalDefaults;

	public InitialiseSteamApiFix(Schema outputSchema, GlobalSettings globalDefaults) {
		super(outputSchema, true);
		this.globalDefaults = globalDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

		return fixTypeEverywhereTyped(
				"Controlify: add initialise steam api global setting",
				type,
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		Dynamic<T> global = root.get("global").orElseEmptyMap();
		if (global.get("initialise_steam_api").result().isPresent()) {
			return root;
		}

		global = global.set("initialise_steam_api", root.createBoolean(globalDefaults.initialiseSteamApi));
		return root.set("global", global);
	}
}
