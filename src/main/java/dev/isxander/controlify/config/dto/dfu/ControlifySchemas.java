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

	public static class SchemaV0 extends Schema {
		public SchemaV0(int versionKey, Schema parent) {
			super(versionKey, parent);
		}

		@Override
		public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
			schema.registerType(
				true,
				ControlifyTypeReferences.USER_STATE,
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

	public static class SchemaV1 extends Schema {
		public SchemaV1(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class SchemaV2 extends Schema {
		public SchemaV2(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class SchemaV3 extends Schema {
		public SchemaV3(int versionKey, Schema parent) {
			super(versionKey, parent);
		}

		@Override
		public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
			super.registerTypes(schema, entityTypes, blockEntityTypes);
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
	}

	public static class SchemaV4 extends Schema {
		public SchemaV4(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}

	public static class SchemaV5 extends Schema {
		public SchemaV5(int versionKey, Schema parent) {
			super(versionKey, parent);
		}
	}
}
