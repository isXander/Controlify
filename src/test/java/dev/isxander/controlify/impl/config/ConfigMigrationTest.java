/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.config.dto.dfu.ControlifyDataFixer;
import dev.isxander.controlify.impl.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.impl.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.impl.controller.gyro.GyroActivation;
import dev.isxander.controlify.impl.controller.gyro.GyroSpace;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Upgrades of profiles written by released versions, which must decode without
/// falling back to defaults and keep what the player configured.
class ConfigMigrationTest {
	private static final String DEFAULT_PROFILE = "/assets/controlify/controller/default_config.json";

	@Test
	void schema8ProfileDecodesWithoutDefaultFallback() throws IOException {
		// decoding the fixed json directly, as ConfigMigrator would silently merge defaults over a failure
		JsonObject fixed = fix(readJson("/config-migrations/profile_v8.json"), 8);
		var decoded = ProfileConfig.CODEC.parse(JsonOps.INSTANCE, fixed);
		assertTrue(decoded.result().isPresent(), () -> "profile did not decode: " + decoded.error());
	}

	@Test
	void schema8ProfilePreservesBindings() throws IOException {
		ProfileConfig profile = migrator().migrateProfile(readJson("/config-migrations/profile_v8.json")).config();
		var bindings = profile.input().bindings().bindings();

		assertEquals(List.of(single("button/east")), bindings.get(id("jump")));
		assertEquals(List.of(single("axis/right_trigger")), bindings.get(id("attack")));
		assertEquals(List.of(single("axis/left_trigger")), bindings.get(id("use")));
		assertEquals(List.of(single("hat/0/up")), bindings.get(id("inventory")));
		assertEquals(
				List.of(ActionBinding.of(new LogicalInput.ModifiedChord(Set.of(id("hat/0/right")), id("hat/0/down")))),
				bindings.get(id("swap_hands"))
		);
		// an unbound action stays unbound instead of inheriting the controller default
		assertEquals(List.of(), bindings.get(id("take_screenshot")));
		assertEquals(List.of(), bindings.get(id("drop")));
	}

	@Test
	void schema8ProfilePreservesGyroAndOtherValues() throws IOException {
		ProfileConfig profile = migrator().migrateProfile(readJson("/config-migrations/profile_v8.json")).config();

		var gyro = profile.gyro();
		assertTrue(gyro.enabled());
		assertEquals(0.75f, gyro.sensitivity());
		assertEquals(GyroActivation.TOGGLE, gyro.activation());
		assertEquals(GyroSpace.LOCAL_BOTH, gyro.space());
		assertTrue(gyro.invertYaw());
		assertFalse(gyro.invertPitch());
		assertTrue(profile.flickStick().enabled());

		assertTrue(profile.generic().autoJump());
		assertFalse(profile.generic().toggleSprint());
		assertEquals(1.4f, profile.input().sensitivity().hLookSensitivity());
		assertTrue(profile.input().sensitivity().vLookInvert());
		assertEquals(0.15f, profile.input().sensitivity().defaultDeadzone());
	}

	@Test
	void disabledGyroStaysDisabled() throws IOException {
		JsonObject source = readJson("/config-migrations/profile_v8.json");
		JsonObject gyro = source.getAsJsonObject("gyro");
		gyro.addProperty("look_sensitivity", 0f);
		gyro.addProperty("button_mode", "off");
		gyro.addProperty("yaw_mode", "roll");
		gyro.addProperty("flick_stick", true);

		ProfileConfig profile = migrator().migrateProfile(source).config();

		assertFalse(profile.gyro().enabled());
		assertEquals(GyroActivation.ALWAYS, profile.gyro().activation());
		assertEquals(GyroSpace.LOCAL_ROLL, profile.gyro().space());
		// flick stick needed gyro sensitivity above zero
		assertFalse(profile.flickStick().enabled());
	}

	@Test
	void schema14BareBindingsAreWrappedInLists() throws IOException {
		// schemas 11 to 14 stored one bare input per action, which the current codec cannot read
		JsonObject source = migrator().migrateProfile(readJson("/config-migrations/profile_v8.json")).json();
		source.addProperty("schema_version", 14);
		JsonObject bindings = source.getAsJsonObject("input").getAsJsonObject("bindings");
		bindings.addProperty("controlify:jump", "controlify:button/east");
		bindings.addProperty("controlify:attack", "controlify:axis/right_trigger");
		bindings.add("controlify:take_screenshot", new JsonObject());
		JsonObject chord = new JsonObject();
		chord.add("modifiers", JsonParser.parseString("[\"controlify:button/left_shoulder\"]"));
		chord.addProperty("trigger", "controlify:button/south");
		bindings.add("controlify:use", chord);

		var migrated = migrator().migrateProfile(source);
		var result = migrated.config().input().bindings().bindings();

		assertTrue(migrated.requiresSaving());
		assertEquals(List.of(single("button/east")), result.get(id("jump")));
		assertEquals(List.of(single("axis/right_trigger")), result.get(id("attack")));
		assertEquals(List.of(), result.get(id("take_screenshot")));
		assertEquals(
				List.of(ActionBinding.of(new LogicalInput.ModifiedChord(Set.of(id("button/left_shoulder")), id("button/south")))),
				result.get(id("use"))
		);
	}

	@Test
	void currentProfileIsNotRewritten() throws IOException {
		JsonObject current = migrator().migrateProfile(readJson("/config-migrations/profile_v8.json")).json();
		var again = migrator().migrateProfile(current);

		assertFalse(again.requiresSaving());
		assertEquals(current, again.json());
	}

	@Test
	void sharedConfigGainsSteamSettingAndLosesGuideScale() throws IOException {
		JsonObject shared = new JsonObject();
		JsonObject global = new JsonObject();
		global.addProperty("ingame_button_guide_scale", 2);
		shared.add("global", global);

		Dynamic<?> fixed = ControlifyDataFixer.createFixer(defaults()).update(
				ControlifyTypeReferences.SHARED_CONFIG,
				new Dynamic<>(JsonOps.INSTANCE, shared),
				13,
				ControlifyDataFixer.CURRENT_VERSION
		);

		JsonObject fixedGlobal = ((JsonObject) fixed.getValue()).getAsJsonObject("global");
		assertTrue(fixedGlobal.has("initialise_steam_api"));
		assertFalse(fixedGlobal.has("ingame_button_guide_scale"));
	}

	private static ActionBinding single(String path) {
		return ActionBinding.of(new LogicalInput.Single(id(path)));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("controlify", path);
	}

	private static JsonObject fix(JsonObject source, int version) throws IOException {
		Dynamic<?> fixed = ControlifyDataFixer.createFixer(defaults()).update(
				ControlifyTypeReferences.PROFILE_CONFIG,
				new Dynamic<>(JsonOps.INSTANCE, source),
				version,
				ControlifyDataFixer.CURRENT_VERSION
		);
		return (JsonObject) fixed.getValue();
	}

	private static ProfileSettings defaults() throws IOException {
		return ProfileSettings.fromDTO(defaultProfile());
	}

	private static ProfileConfig defaultProfile() throws IOException {
		var result = ProfileConfig.CODEC.parse(JsonOps.INSTANCE, readJson(DEFAULT_PROFILE));
		return result.result().orElseThrow(() -> new IOException("Failed to decode default profile: " + result.error()));
	}

	private static ConfigMigrator migrator() throws IOException {
		return new ConfigMigrator(defaultProfile());
	}

	private static JsonObject readJson(String resourcePath) throws IOException {
		var input = ConfigMigrationTest.class.getResourceAsStream(resourcePath);
		assertNotNull(input, "Missing test resource " + resourcePath);
		try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
