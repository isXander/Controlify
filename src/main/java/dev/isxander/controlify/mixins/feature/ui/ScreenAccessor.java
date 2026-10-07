/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Screen.class)
public interface ScreenAccessor {
	@Accessor("INWORLD_HEADER_SEPARATOR")
	static Identifier controlify$getInworldHeaderSeparator() {
		throw new AssertionError();
	}

	@Accessor("INWORLD_FOOTER_SEPARATOR")
	static Identifier controlify$getInworldFooterSeparator() {
		throw new AssertionError();
	}
}
