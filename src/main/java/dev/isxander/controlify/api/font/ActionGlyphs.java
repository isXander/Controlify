/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.font;

import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.impl.font.ActionGlyphsImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/// Creates text components whose glyphs are resolved by each receiving Controlify client.
///
/// Components identify an action rather than a controller or a fixed input. When displayed,
/// the client resolves the action's current binding using its selected controller and glyph set.
/// The same component can be used locally or sent by a server to multiple clients.
/// Creating it does not require a controller, a registered action, or client initialization;
/// the action must exist on the receiving client for its binding to be displayed.
///
/// These factories only use common Minecraft classes and may be called on a dedicated server.
/// They preserve the component's action reference across vanilla component serialization.
/// Use a fallback variant for components that may reach clients without Controlify.
public final class ActionGlyphs {
	private ActionGlyphs() {
	}

	/// Creates a glyph component for an action, regardless of the client's current input mode.
	/// Without a selected controller or Controlify, the keybind text is displayed instead;
	/// use {@link #actionWithFallback(Identifier, Component)} for a readable alternative.
	public static Component action(Identifier action) {
		return ActionGlyphsImpl.action(action);
	}

	/// Equivalent to {@link #action(Identifier)}, using only the accessor's action identifier.
	/// This does not resolve or retain a controller-specific action instance.
	public static Component action(ActionAccessor action) {
		return action(action.actionId());
	}

	/// Creates a glyph component that displays {@code fallback} on clients without Controlify.
	/// Controlify clients select the glyph branch regardless of their current input mode.
	/// This fallback does not handle missing actions, unbound actions, or a missing controller.
	/// Both alternatives are included in the serialized component.
	public static Component actionWithFallback(Identifier action, Component fallback) {
		return ActionGlyphsImpl.actionWithFallback(action, fallback);
	}

	/// Equivalent to {@link #actionWithFallback(Identifier, Component)}, using the accessor's identifier.
	public static Component actionWithFallback(ActionAccessor action, Component fallback) {
		return actionWithFallback(action.actionId(), fallback);
	}

	/// Creates a glyph component that displays {@code fallback} unless the receiving client
	/// has Controlify and is using controller input mode. This is useful for messages that
	/// should show an ordinary keybind or text to keyboard and mouse users.
	/// This fallback does not handle missing or unbound actions.
	/// Both alternatives are included in the serialized component.
	public static Component actionWithActiveFallback(Identifier action, Component fallback) {
		return ActionGlyphsImpl.actionWithActiveFallback(action, fallback);
	}

	/// Equivalent to {@link #actionWithActiveFallback(Identifier, Component)}, using the accessor's identifier.
	public static Component actionWithActiveFallback(ActionAccessor action, Component fallback) {
		return actionWithActiveFallback(action.actionId(), fallback);
	}
}
