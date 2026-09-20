/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class ConfigMigrationTestHelper {
	private static final String DEFAULT_PROFILE = "/assets/controlify/controllers/default_config.json";

	public static void assertSharedMigration(String input, String expected) throws IOException {
		var actual = migrator().migrateShared(readJson(input)).json();
		Assertions.assertEquals(readJson(expected), actual);
	}

	public static void assertProfileMigration(String input, String expected) throws IOException {
		var actual = migrator().migrateProfile(readJson(input)).json();
		Assertions.assertEquals(readJson(expected), actual);
	}

	public static void assertLegacyMigration(
		String input,
		String expectedShared,
		String... expectedProfiles
	) throws IOException {
		var actual = migrator().migrateLegacy(readJson(input));
		Assertions.assertEquals(readJson(expectedShared), actual.shared().json());
		Assertions.assertEquals(expectedProfiles.length, actual.profiles().size());
		for (int index = 0; index < expectedProfiles.length; index++) {
			Assertions.assertEquals(readJson(expectedProfiles[index]), actual.profiles().get(index).json());
		}
	}

	public static ConfigMigrator migrator() throws IOException {
		var result = ProfileConfig.CODEC.parse(JsonOps.INSTANCE, readJson(DEFAULT_PROFILE));
		var defaults = result.result().orElseThrow(() -> new IOException(
			"Failed to decode test default profile: "
				+ result.error().map(error -> error.message()).orElse("unknown error")
		));
		return new ConfigMigrator(defaults);
	}

	public static JsonObject readJson(String resourcePath) throws IOException {
		String absolutePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
		var input = ConfigMigrationTestHelper.class.getResourceAsStream(absolutePath);
		if (input == null) {
			throw new IOException("Missing config migration test resource " + absolutePath);
		}
		try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private ConfigMigrationTestHelper() {
	}
}
