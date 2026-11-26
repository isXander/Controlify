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

import java.util.ArrayList;
import java.util.List;

/// Deadzone groups changed from a flat axis list to opposing half-axis pairs.
/// Flat lists were conventionally ordered positive then negative for each dimension,
/// so consecutive axes become a dimension; a trailing odd axis becomes a one-sided dimension.
public final class DeadzoneDimensionsFix extends DataFix {
	public DeadzoneDimensionsFix(Schema outputSchema) {
		super(outputSchema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped(
				"Controlify: pair device mapping deadzone axes into dimensions",
				getInputSchema().getType(ControlifyTypeReferences.SHARED_CONFIG),
				typed -> typed.update(DSL.remainderFinder(), root -> root.update("devices", devices ->
						devices.updateMapValues(entry -> entry.mapSecond(device ->
								device.update("mapping", mapping ->
										mapping.update("deadzones", deadzones ->
												deadzones.updateMapValues(group -> group.mapSecond(this::rewriteGroup))))))))
		);
	}

	private <T> Dynamic<T> rewriteGroup(Dynamic<T> group) {
		var axes = group.get("axes").asList(axis -> axis);
		if (axes.isEmpty()) {
			return group;
		}

		List<Dynamic<T>> dimensions = new ArrayList<>();
		for (int i = 0; i < axes.size(); i += 2) {
			var dimension = group.emptyMap().set("positive", axes.get(i));
			if (i + 1 < axes.size()) {
				dimension = dimension.set("negative", axes.get(i + 1));
			}
			dimensions.add(dimension);
		}

		return group.remove("axes").set("dimensions", group.createList(dimensions.stream()));
	}
}
