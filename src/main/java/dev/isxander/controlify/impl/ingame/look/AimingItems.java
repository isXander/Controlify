/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.ingame.look;

import net.minecraft.world.item.ItemUseAnimation;

/// Which items being used count as aiming, for look sources that slow down or change behaviour while aiming.
final class AimingItems {
	/// A crossbow is not included: using it loads the bolt rather than aiming.
	static boolean isAiming(ItemUseAnimation animation) {
		return switch (animation) {
			case BOW, TRIDENT, SPEAR, SPYGLASS -> true;
			default -> false;
		};
	}

	private AimingItems() {
	}
}
