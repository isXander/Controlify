/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop.behaviours.element;

/// Private adjustment hooks used by the shared slider and cycle behaviours.
public interface ControlValueAccess {
	void controlify$adjustValue(int direction);
}
