/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.SharedConfig;
import dev.isxander.controlify.config.dto.dfu.ControlifyDataFixer;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.config.dto.profile.defaults.DefaultConfigManager;
import dev.isxander.controlify.config.settings.ControlifySettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Converts historical config JSON into canonical, current-schema config DTOs. */
public final class ConfigMigrator {
	private static final int LAST_LEGACY_SCHEMA_VERSION = 2;

	private final ProfileConfig defaultProfile;
	private final DataFixer dataFixer;

	public ConfigMigrator(ProfileConfig defaultProfile) {
		this(
			defaultProfile,
			ControlifyDataFixer.createFixer(ProfileSettings.fromDTO(defaultProfile))
		);
	}

	ConfigMigrator(ProfileConfig defaultProfile, DataFixer dataFixer) {
		this.defaultProfile = defaultProfile;
		this.dataFixer = dataFixer;
	}

	public MigrationResult<SharedConfig> migrateShared(JsonObject source) throws IOException {
		int schemaVersion = schemaVersion(source);
		validateSplitSchemaVersion(schemaVersion);
		return this.migrate(
			source,
			ControlifyTypeReferences.SHARED_CONFIG,
			schemaVersion,
			SharedConfig.CODEC,
			this::completeShared,
			"shared config"
		);
	}

	public MigrationResult<ProfileConfig> migrateProfile(JsonObject source) throws IOException {
		int schemaVersion = schemaVersion(source);
		validateSplitSchemaVersion(schemaVersion);
		return this.migrate(
			source,
			ControlifyTypeReferences.PROFILE_CONFIG,
			schemaVersion,
			ProfileConfig.CODEC,
			this::completeProfile,
			"profile config"
		);
	}

	public LegacyMigrationResult migrateLegacy(JsonObject source) throws IOException {
		int schemaVersion = schemaVersion(source);
		if (schemaVersion > LAST_LEGACY_SCHEMA_VERSION) {
			throw new IOException("Unsupported legacy Controlify config schema " + schemaVersion);
		}

		JsonObject fixedLegacy = this.fix(source, ControlifyTypeReferences.USER_STATE, schemaVersion);
		MigrationResult<SharedConfig> shared = this.migrateFixed(
			this.fix(fixedLegacy.deepCopy(), ControlifyTypeReferences.SHARED_CONFIG, schemaVersion),
			SharedConfig.CODEC,
			this::completeShared,
			"legacy shared config",
			true
		);

		JsonElement profilesElement = fixedLegacy.get("profiles");
		if (profilesElement != null && !profilesElement.isJsonArray()) {
			throw new IOException("Failed to decode legacy config: profiles is not a list");
		}

		JsonArray profiles = profilesElement == null ? new JsonArray() : profilesElement.getAsJsonArray();
		List<MigrationResult<ProfileConfig>> migratedProfiles = new ArrayList<>(profiles.size());
		for (int index = 0; index < profiles.size(); index++) {
			JsonElement profile = profiles.get(index);
			if (!profile.isJsonObject()) {
				throw new IOException("Failed to decode legacy config: profile " + index + " is not an object");
			}
			migratedProfiles.add(this.migrateFixed(
				this.fix(profile.getAsJsonObject(), ControlifyTypeReferences.PROFILE_CONFIG, schemaVersion),
				ProfileConfig.CODEC,
				this::completeProfile,
				"legacy profile " + index,
				true
			));
		}

		return new LegacyMigrationResult(shared, List.copyOf(migratedProfiles));
	}

	private <T> MigrationResult<T> migrate(
		JsonObject source,
		DSL.TypeReference type,
		int schemaVersion,
		Codec<T> codec,
		JsonCompleter completer,
		String description
	) throws IOException {
		JsonObject fixed = this.fix(source, type, schemaVersion);
		return this.migrateFixed(
			fixed,
			codec,
			completer,
			description,
			schemaVersion != ControlifyDataFixer.CURRENT_VERSION
		);
	}

	private <T> MigrationResult<T> migrateFixed(
		JsonObject fixed,
		Codec<T> codec,
		JsonCompleter completer,
		String description,
		boolean requiresSaving
	) throws IOException {
		DataResult<T> initialResult = codec.parse(JsonOps.INSTANCE, fixed);
		T config;
		if (initialResult.isError()) {
			config = decode(codec, completer.complete(fixed), description);
			requiresSaving = true;
		} else {
			config = initialResult.result().orElseThrow();
		}

		return new MigrationResult<>(encodeCurrent(codec, config, description), config, requiresSaving);
	}

	private JsonObject completeProfile(JsonObject source) {
		JsonObject completed = encode(ProfileConfig.CODEC, this.defaultProfile).getAsJsonObject();
		DefaultConfigManager.mergeJsonObjects(completed, source);
		return completed;
	}

	private JsonObject completeShared(JsonObject source) {
		JsonObject completed = encode(SharedConfig.CODEC, ControlifySettings.defaults().toSharedDTO()).getAsJsonObject();
		DefaultConfigManager.mergeJsonObjects(completed, source);
		return completed;
	}

	private JsonObject fix(JsonObject source, DSL.TypeReference type, int schemaVersion) {
		Dynamic<?> fixed = this.dataFixer.update(
			type,
			new Dynamic<>(JsonOps.INSTANCE, source.deepCopy()),
			schemaVersion,
			ControlifyDataFixer.CURRENT_VERSION
		);
		return (JsonObject) fixed.getValue();
	}

	private static <T> JsonObject encodeCurrent(Codec<T> codec, T config, String description) throws IOException {
		JsonObject root = new JsonObject();
		root.addProperty("schema_version", ControlifyDataFixer.CURRENT_VERSION);
		JsonElement encoded = codec.encodeStart(JsonOps.INSTANCE, config)
			.result()
			.orElseThrow(() -> new IOException("Failed to encode migrated " + description));
		encoded.getAsJsonObject().entrySet().forEach(entry -> root.add(entry.getKey(), entry.getValue()));
		return root;
	}

	private static <T> JsonElement encode(Codec<T> codec, T config) {
		return codec.encodeStart(JsonOps.INSTANCE, config).result().orElseThrow();
	}

	private static <T> T decode(Codec<T> codec, JsonObject json, String description) throws IOException {
		DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
		return result.result().orElseThrow(() -> new IOException(
			"Failed to decode " + description + ": "
				+ result.error().map(DataResult.Error::message).orElse("unknown error")
		));
	}

	private static int schemaVersion(JsonObject root) {
		return root.has("schema_version") ? root.get("schema_version").getAsInt() : 0;
	}

	private static void validateSplitSchemaVersion(int schemaVersion) throws IOException {
		if (schemaVersion <= LAST_LEGACY_SCHEMA_VERSION || schemaVersion > ControlifyDataFixer.CURRENT_VERSION) {
			throw new IOException("Unsupported split Controlify schema " + schemaVersion);
		}
	}

	public record MigrationResult<T>(JsonObject json, T config, boolean requiresSaving) {
	}

	public record LegacyMigrationResult(
		MigrationResult<SharedConfig> shared,
		List<MigrationResult<ProfileConfig>> profiles
	) {
	}

	@FunctionalInterface
	private interface JsonCompleter {
		JsonObject complete(JsonObject source);
	}
}
