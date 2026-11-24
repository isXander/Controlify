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

/// The global in-game button guide scale is superseded by the per-profile guide gui scale.
public final class RemoveGlobalGuideScaleFix extends DataFix {
	public RemoveGlobalGuideScaleFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var type = getInputSchema().getType(ControlifyTypeReferences.SHARED_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: remove global ingame button guide scale",
				type,
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		Dynamic<T> global = root.get("global").orElseEmptyMap().remove("ingame_button_guide_scale");
		return root.set("global", global);
	}
}
