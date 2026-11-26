/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.steamdeck;

import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

public class SteamDeckComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("steam_deck");

	public SteamDeckComponent() {
		super(ID);
	}
}
