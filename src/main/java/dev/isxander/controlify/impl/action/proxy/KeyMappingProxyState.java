/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.proxy;

/// Held state for one key target, independent of the source action's latch.
final class KeyMappingProxyState {
	private boolean down;
	private boolean toggleMode;

	boolean isDown() {
		return this.down;
	}

	void update(boolean toggleMode, boolean sourceDown, int pulses) {
		if (!toggleMode) {
			this.down = sourceDown;
		} else {
			if (!this.toggleMode) {
				this.down = false;
			}
			if ((pulses & 1) != 0) {
				this.down = !this.down;
			}
		}
		this.toggleMode = toggleMode;
	}

	void reset() {
		this.down = false;
		this.toggleMode = false;
	}
}
