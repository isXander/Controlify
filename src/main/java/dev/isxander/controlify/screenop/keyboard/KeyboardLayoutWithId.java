/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.screenop.keyboard;

import net.minecraft.resources.Identifier;

/**
 * Represents a keyboard layout with an associated ID.
 * Layouts themselves do not have IDs, but this record
 * couples a layout with its resource pack ID.
 * @param layout the keyboard layout
 * @param id the resource location ID of the layout
 */
public record KeyboardLayoutWithId(KeyboardLayout layout, Identifier id) {
}
