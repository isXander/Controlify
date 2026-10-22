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
import dev.isxander.controlify.impl.config.dto.dfu.ControlifyTypeReferences;

public class RemoveRelativeGyroFix extends DataFix {
	public RemoveRelativeGyroFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: remove relative gyro mode",
				getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG),
				typed -> typed.update(DSL.remainderFinder(),
						root -> root.update("gyro", gyro -> gyro.remove("relative_mode")))
		);
	}
}
