/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// IDs of Controlify's built-in action layers. Use these directly in {@link ActionSpecBuilder#layer(Identifier...)}.
/// Referencing these constants does not initialize or register layer implementations.
///
/// The built-in stack, from top to bottom:
/// {@link #UNKNOWN}, {@link #RADIAL_MENU}, {@link #V_MOUSE_COMPAT}, {@link #V_MOUSE_CURSOR},
/// {@link #CONTAINER}, {@link #REGULAR_SCREEN}, {@link #ANY_SCREEN}, {@link #IN_GAME}.
/// Use these as anchors with {@link ActionLayerRegistry.Builder#above(Identifier)} and
/// {@link ActionLayerRegistry.Builder#below(Identifier)}.
public final class BuiltinActionLayers {
	/// Always applicable. Transparent, at the top.
	public static final Identifier UNKNOWN = CUtil.rl("unknown");
	/// A world is loaded. Overlay, at the bottom; hidden whenever a screen is open.
	public static final Identifier IN_GAME = CUtil.rl("in_game");
	/// Any screen is open. Blocking, so gameplay contexts below it receive nothing.
	public static final Identifier ANY_SCREEN = CUtil.rl("screen");
	/// A screen is open and the virtual mouse is off. Transparent.
	public static final Identifier REGULAR_SCREEN = CUtil.rl("regular_screen");
	/// A container screen is open. Transparent.
	public static final Identifier CONTAINER = CUtil.rl("container");
	/// The virtual mouse is on. Transparent.
	public static final Identifier V_MOUSE_CURSOR = CUtil.rl("vmouse_cursor");
	/// The virtual mouse is on and emulating clicks. Transparent.
	public static final Identifier V_MOUSE_COMPAT = CUtil.rl("vmouse_compat");
	/// The radial menu is open. Overlay, so its inputs shadow the screen contexts below.
	public static final Identifier RADIAL_MENU = CUtil.rl("radial_menu");

	private BuiltinActionLayers() {
	}
}
