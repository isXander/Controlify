/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.defaults.DefaultBindManager;
import dev.isxander.controlify.impl.contextual.FactDefinition;
import dev.isxander.controlify.impl.contextual.FeedbackRule;
import dev.isxander.controlify.impl.contextual.GuideRule;
import dev.isxander.controlify.impl.contextual.RuleSet;
import dev.isxander.controlify.impl.contextual.TriggerEffectRule;
import dev.isxander.controlify.impl.feedback.FeedbackGroups;
import dev.isxander.controlify.impl.feedback.data.DataFeedbackEffect;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardLayout;
import dev.isxander.controlify.impl.gui.radial.RadialIcon;
import dev.isxander.controlify.impl.input.mapping.ControllerMapping;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/// Decodes every resource file Controlify and the example addon ship, with the codec Controlify loads it with.
/// The docs import these files, so a file that stops decoding would also be a wrong example.
/// Files are parsed leniently, like the loaders do, so they may contain `// #region` comments.
class ResourceFormatsTest {
	/// Set by the test task: the resource directories to check, separated by the path separator.
	private static final String ROOTS_PROPERTY = "controlify.test.resourceRoots";

	private record Format(String name, String glob, Codec<?> codec) {
	}

	private static final List<Format> FORMATS = List.of(
			new Format("activators", "assets/*/controller/activators.json",
					Codec.unboundedMap(Identifier.CODEC, ActivatorConfig.CODEC)),
			new Format("default binds", "assets/*/controller/default_bind/**.json",
					DefaultBindManager.ControllerDefault.CODEC),
			new Format("radial icons", "assets/*/controller/radial_icons.json",
					Codec.unboundedMap(Identifier.CODEC, RadialIcon.CODEC)),
			new Format("feedback effect", "assets/*/controller/feedback/effect/**.json",
					DataFeedbackEffect.CODEC),
			new Format("keyboard layout", "assets/*/controller/keyboard_layout/**.json",
					KeyboardLayout.CODEC),
			new Format("joystick mapping", "assets/*/controller/mapping/**.json",
					ControllerMapping.CODEC),
			new Format("contextual facts", "assets/*/controller/contextual/facts/*.json",
					FactDefinition.LIST_CODEC),
			new Format("guide rules", "assets/*/controller/contextual/guide/*.json",
					RuleSet.createCodec(GuideRule.CODEC)),
			new Format("trigger effect rules", "assets/*/controller/contextual/trigger_effect/*.json",
					RuleSet.createCodec(TriggerEffectRule.CODEC)),
			new Format("feedback rules", "assets/*/controller/contextual/feedback/*.json",
					RuleSet.createCodec(FeedbackRule.CODEC))
	);

	/// Text components, used by keyboard layouts and radial icon overlays, need the registries.
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@TestFactory
	Stream<DynamicTest> everyResourceDecodes() {
		String roots = System.getProperty(ROOTS_PROPERTY);
		if (roots == null) {
			fail("System property " + ROOTS_PROPERTY + " is not set; run through the Gradle test task");
		}

		List<DynamicTest> tests = new ArrayList<>();
		for (String rootString : roots.split(java.io.File.pathSeparator)) {
			Path root = Path.of(rootString);
			for (Format format : FORMATS) {
				for (Path file : matching(root, format.glob())) {
					String name = format.name() + ": " + root.getParent().getFileName() + "/" + root.relativize(file);
					tests.add(DynamicTest.dynamicTest(name, () -> decode(file, format.codec())));
				}
			}
		}
		assertFalse(tests.isEmpty(), "No resource files found under " + roots);
		return tests.stream();
	}

	/// Feedback groups only make sense merged, since a pack's ordering can name another pack's groups.
	/// The roots are merged in order, like resource packs from lowest to highest priority.
	@Test
	void feedbackGroupsResolveWithoutWarnings() throws IOException {
		List<FeedbackGroups.PackFile> files = new ArrayList<>();
		for (Path root : roots()) {
			for (Path file : matching(root, "assets/*/controller/feedback/groups.json")) {
				try (Reader reader = Files.newBufferedReader(file)) {
					files.add(new FeedbackGroups.PackFile(root.relativize(file).toString(), JsonParser.parseReader(reader)));
				}
			}
		}
		assertFalse(files.isEmpty(), "No groups.json found");

		List<String> warnings = new ArrayList<>();
		FeedbackGroups.resolve(files, new FeedbackGroups.Reporter() {
			@Override
			public void warn(String message) {
				warnings.add(message);
			}

			@Override
			public void debug(String message) {
			}
		});
		if (!warnings.isEmpty()) {
			fail(String.join("\n", warnings));
		}
	}

	private static List<Path> roots() {
		String roots = System.getProperty(ROOTS_PROPERTY);
		if (roots == null) {
			fail("System property " + ROOTS_PROPERTY + " is not set; run through the Gradle test task");
		}
		return Stream.of(roots.split(java.io.File.pathSeparator)).map(Path::of).toList();
	}

	private static void decode(Path file, Codec<?> codec) throws IOException {
		JsonElement json;
		try (Reader reader = Files.newBufferedReader(file)) {
			json = JsonParser.parseReader(reader);
		}
		// like the contextual loaders, so registry references (such as tags) decode as placeholders
		DynamicOps<JsonElement> ops = new PlaceholderLookupProvider(RegistryAccess.EMPTY).createSerializationContext(JsonOps.INSTANCE);
		DataResult<?> result = codec.parse(ops, json);
		result.error().ifPresent(error -> fail(error.message()));
	}

	private static List<Path> matching(Path root, String glob) {
		if (!Files.isDirectory(root)) {
			return List.of();
		}
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(Files::isRegularFile)
					.filter(file -> matcher.matches(root.relativize(file)))
					.sorted()
					.toList();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
