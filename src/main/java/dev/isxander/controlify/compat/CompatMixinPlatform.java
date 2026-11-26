/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat;

@FunctionalInterface
public interface CompatMixinPlatform {
	boolean isModLoaded(String modId);
}
