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
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

public final class DualsenseConfigFix extends DataFix {
	private final ProfileSettings profileDefaults;

	public DualsenseConfigFix(Schema outputSchema, ProfileSettings profileDefaults) {
		super(outputSchema, true);
		this.profileDefaults = profileDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: add DualSense profile config",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		Dynamic<T> dualsense = root.get("dualsense").orElseEmptyMap();
		if (dualsense.get("trigger_effects").result().isEmpty()) {
			dualsense = dualsense.set("trigger_effects", root.createBoolean(profileDefaults.dualsense.triggerEffects));
		}
		return root.set("dualsense", dualsense);
	}
}
