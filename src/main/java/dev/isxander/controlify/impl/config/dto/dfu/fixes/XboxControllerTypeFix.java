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

/// Renames the remembered `controlify:xbox_one` controller type of each device to `controlify:xbox`.
public final class XboxControllerTypeFix extends DataFix {
	private static final String OLD_TYPE = "controlify:xbox_one";
	private static final String NEW_TYPE = "controlify:xbox";

	public XboxControllerTypeFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: rename xbox_one controller type to xbox",
				getInputSchema().getType(ControlifyTypeReferences.SHARED_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), root -> root.update("devices", devices ->
						devices.updateMapValues(entry -> entry.mapSecond(this::rewriteDevice))))
		);
	}

	private <T> Dynamic<T> rewriteDevice(Dynamic<T> device) {
		return device.update("controller_type", XboxControllerTypeFix::rewriteType);
	}

	private static <T> Dynamic<T> rewriteType(Dynamic<T> type) {
		return OLD_TYPE.equals(type.asString().result().orElse(null))
				? type.createString(NEW_TYPE)
				: type;
	}
}
