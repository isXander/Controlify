/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.activator.PulseLeafConfig;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BindingConflictsTest {
	private static final LogicalInput SOUTH = new LogicalInput.Single(id("button/south"));

	private static final ActionLayer MENU = layer("menu", ActionLayerMode.OVERLAY, "screen");
	private static final ActionLayer HUD = layer("hud", ActionLayerMode.OVERLAY);
	private static final ActionLayer WIDGET = layer("widget", ActionLayerMode.TRANSPARENT, "screen");
	private static final ActionLayer SCREEN = layer("screen", ActionLayerMode.BLOCKING);
	private static final ActionLayer GAME = layer("game", ActionLayerMode.OVERLAY);
	private static final List<ActionLayer> ORDER = List.of(MENU, HUD, WIDGET, SCREEN, GAME);

	@Test
	void sharedLayerConflicts() {
		assertEquals(List.of(BindingConflicts.Kind.CONFLICT), kinds(GAME, GAME));
	}

	@Test
	void overlayShadowsLayersItCanReach() {
		assertEquals(List.of(BindingConflicts.Kind.SHADOWED), kinds(WIDGET, MENU));
		assertEquals(List.of(BindingConflicts.Kind.SHADOWED), kinds(SCREEN, MENU));
		// Nothing says a screen is open while the HUD layer is active.
		assertEquals(List.of(BindingConflicts.Kind.SHADOWED), kinds(GAME, HUD));
	}

	@Test
	void blockingLayerDoesNotShadowWhatItHides() {
		assertEquals(List.of(), kinds(GAME, SCREEN));
	}

	@Test
	void overlayDoesNotShadowBelowABlockingLayerItRequires() {
		assertEquals(List.of(), kinds(GAME, MENU));
	}

	@Test
	void transparentLayerNeverShadows() {
		assertEquals(List.of(), kinds(SCREEN, WIDGET));
	}

	/// The conflicts reported on an action in {@code lower} against one in {@code upper} sharing its input.
	private static List<BindingConflicts.Kind> kinds(ActionLayer lower, ActionLayer upper) {
		var entries = List.of(entry("lower", lower), entry("upper", upper));
		return BindingConflicts.find(entries, ORDER).stream()
				.filter(conflict -> conflict.action().equals(id("lower")))
				.map(BindingConflicts.Conflict::kind)
				.toList();
	}

	private static BindingConflicts.Entry entry(String name, ActionLayer layer) {
		return new BindingConflicts.Entry(id(name), Set.of(layer.id()), List.of(ActionBinding.of(SOUTH)), PulseLeafConfig.DEFAULT);
	}

	private static ActionLayer layer(String name, ActionLayerMode mode, String... requires) {
		return new ActionLayer(id(name), mode, _ -> true,
				Set.copyOf(java.util.Arrays.stream(requires).map(BindingConflictsTest::id).toList()));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
