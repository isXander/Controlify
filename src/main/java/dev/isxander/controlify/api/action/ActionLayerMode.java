/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

/// How an active layer affects the layers ordered below it.
public enum ActionLayerMode {
	/// Consumes nothing. Layers below receive every input, including inputs this layer binds.
	/// Actions sharing an input with lower layers all receive it.
	TRANSPARENT,
	/// Consumes only the inputs this layer's actions are bound to.
	/// Layers below still receive every other input.
	OVERLAY,
	/// Consumes every input. No layer below receives anything while this layer is active.
	BLOCKING,
}
