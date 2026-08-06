/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.bindings;

import dev.isxander.controlify.utils.CUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

/// @deprecated Radial icons are now data driven.
/// [Check out the wiki](https://moddedmc.wiki/en/project/controlify/latest/docs/resource-packs/radial-icons)
@Deprecated(forRemoval = true)
public final class RadialIcons {

	private RadialIcons() {
	}

	@Deprecated(forRemoval = true)
	public static Identifier getItem(Item item) {
		return CUtil.rl("radial_icons_are_data_driven");
	}

	@Deprecated(forRemoval = true)
	public static Identifier getEffect(Holder<MobEffect> effect) {
		return CUtil.rl("radial_icons_are_data_driven");
	}
}
