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

import java.util.stream.Stream;

public final class DeviceInputFormatFix extends DataFix {
	public DeviceInputFormatFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: migrate device gyro offsets and discard obsolete mappings",
				getInputSchema().getType(ControlifyTypeReferences.SHARED_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), root -> root.update("devices", devices ->
						devices.updateMapValues(entry -> entry.mapSecond(this::rewriteDevice))))
		);
	}

	private <T> Dynamic<T> rewriteDevice(Dynamic<T> device) {
		return device.update("gyro_calibration", calibration -> calibration.update("offset", offset -> {
			if (offset.getMapValues().result().isEmpty()) return offset;
			return offset.createList(Stream.of("pitch", "yaw", "roll")
					.map(axis -> offset.createFloat(offset.get(axis).asFloat(0f))));
		}))
				// Snapshot mappings have different semantics from the new event mappings.
				// Discard the old override so the controller uses its current default mapping.
				.remove("mapping");
	}
}
