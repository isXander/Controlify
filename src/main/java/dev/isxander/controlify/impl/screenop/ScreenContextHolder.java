/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import org.jetbrains.annotations.Nullable;

/// Storage only. All sidecar selection is performed through the registries.
public interface ScreenContextHolder {
	ScreenContextImpl<?> controlify$screenContext();

	@Nullable ScreenContextImpl<?> controlify$existingScreenContext();
}
