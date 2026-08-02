/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public final class ControlifySchemas {
	private ControlifySchemas() {
	}

	public static class V0 extends Schema {
		public V0(int versionKey, Schema parent) {
			super(versionKey, parent);
		}

		@Override
		public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
			// Keep independently migrated payload types addressable from every source version.
			schema.registerType(
					true,
					ControlifyTypeReferences.USER_STATE,
					DSL::remainder
			);
			schema.registerType(
					true,
					ControlifyTypeReferences.SHARED_CONFIG,
					DSL::remainder
			);
			schema.registerType(
					true,
					ControlifyTypeReferences.PROFILE_CONFIG,
					DSL::remainder
			);
		}

		@Override
		public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
			return Map.of();
		}

		@Override
		public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
			return Map.of();
		}
	}

	public static class V1 extends Schema {
		public V1(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class V2 extends Schema {
		public V2(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class V3 extends Schema {
		public V3(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class V6 extends Schema {
		public V6(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class V7 extends Schema {
		public V7(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}
}
