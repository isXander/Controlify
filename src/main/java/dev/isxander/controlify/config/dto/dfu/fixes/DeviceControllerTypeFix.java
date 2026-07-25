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
import net.minecraft.resources.Identifier;

public final class DeviceControllerTypeFix extends DataFix {
	private final String defaultControllerType;

	public DeviceControllerTypeFix(Schema outputSchema, Identifier defaultControllerType) {
		super(outputSchema, true);
		this.defaultControllerType = defaultControllerType.toString();
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var type = getInputSchema().getType(ControlifyTypeReferences.SHARED_CONFIG);
		return fixTypeEverywhereTyped(
				"Controlify: add controller type to remembered devices",
				type,
				typed -> typed.update(DSL.remainderFinder(), this::rewrite)
		);
	}

	private <T> Dynamic<T> rewrite(Dynamic<T> root) {
		return root.update(
				"devices",
				devices -> devices.updateMapValues(entry -> {
					Dynamic<?> device = entry.getSecond();
					Dynamic<?> fixedDevice = device.get("controller_type").result().isEmpty()
							? device.set("controller_type", device.createString(defaultControllerType))
							: device;
					return entry.mapSecond(ignored -> fixedDevice);
				})
		);
	}
}
