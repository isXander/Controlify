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

public final class LedEffectsFix extends DataFix {
	private final ProfileSettings profileDefaults;

	public LedEffectsFix(Schema outputSchema, ProfileSettings profileDefaults) {
		super(outputSchema, true);
		this.profileDefaults = profileDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: add LED effects setting",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		if (root.get("light").result().isPresent()) {
			return root;
		}
		Dynamic<T> light = root.emptyMap()
				.set("enabled", root.createBoolean(profileDefaults.light.enabled));
		return root.set("light", light);
	}
}
