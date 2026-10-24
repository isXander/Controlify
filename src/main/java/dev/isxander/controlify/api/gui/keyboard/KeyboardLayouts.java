/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.gui.keyboard;

import net.minecraft.resources.Identifier;

/// Resource-backed keyboard layout identifiers. Custom layouts use their own identifier.
public final class KeyboardLayouts {
	public static final Identifier FULL = Identifier.fromNamespaceAndPath("controlify", "full");
	public static final Identifier SIMPLE = Identifier.fromNamespaceAndPath("controlify", "simple");
	public static final Identifier SERVER_IP = Identifier.fromNamespaceAndPath("controlify", "server_ip");

	private KeyboardLayouts() {
	}
}
