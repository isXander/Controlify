/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/// Screen access that moved between Minecraft versions. Not used by any docs snippet.
public final class Screens {
	public static void set(@Nullable Screen screen) {
		//? if >=26.2 {
		Minecraft.getInstance().gui.setScreen(screen);
		//?} else {
		//Minecraft.getInstance().setScreen(screen);
		//?}
	}

	public static @Nullable Screen current() {
		//? if >=26.2 {
		return Minecraft.getInstance().gui.screen();
		//?} else {
		//return Minecraft.getInstance().screen;
		//?}
	}

	private Screens() {
	}
}
