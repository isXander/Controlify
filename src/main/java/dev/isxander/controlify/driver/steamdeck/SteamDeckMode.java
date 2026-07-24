/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.steamdeck;

public enum SteamDeckMode {
	GAMING_MODE,
	DESKTOP_MODE,
	NOT_STEAM_DECK;

	public boolean isSteamDeck() {
		return this != NOT_STEAM_DECK;
	}
	public boolean isGamingMode() {
		return this == GAMING_MODE;
	}
	public boolean isDesktopMode() {
		return this == DESKTOP_MODE;
	}
}
