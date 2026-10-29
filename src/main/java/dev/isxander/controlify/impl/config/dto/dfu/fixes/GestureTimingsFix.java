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
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;

public class GestureTimingsFix extends DataFix {
	private final ProfileSettings profileDefaults;

	public GestureTimingsFix(Schema outputSchema, ProfileSettings profileDefaults) {
		super(outputSchema, true);
		this.profileDefaults = profileDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: add button gesture timing settings",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		var defaults = profileDefaults.input.gestures;
		var input = root.get("input").orElseEmptyMap();

		var gestures = root.emptyMap()
				.set("tap_max_ms", root.createInt(defaults.tapMaxMs))
				.set("double_tap_gap_ms", root.createInt(defaults.doubleTapGapMs))
				.set("hold_ms", root.createInt(defaults.holdMs));

		return root.set("input", input.set("gestures", gestures));
	}
}
