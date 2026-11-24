/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.steam;

import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.steam.SteamApi;
import dev.isxander.steam.ffm.SteamApiFfm;

public final class SteamHelper {

	private SteamHelper() {
	}

	/// Steam API initialization is required when the game is launched
	/// without Steam launching it. This is because without initialization,
	/// the Steam Client forces keyboard/mouse emulation for Steam Controller/Deck.
	public static void initializeSteam() {
		boolean shouldInitialiseSteam = true;
		boolean useSpacewar = false;

		String steamGameIdStr = System.getenv("SteamGameId");
		if (steamGameIdStr != null) {
			long steamGameId = Long.parseUnsignedLong(steamGameIdStr);
			boolean isShortcut = ((steamGameId >>> 24) & 0xFF) == 0x2;
			// if by some miracle someone manages to get a minecraft launcher distributed on steam,
			// only prevent initialization if the game id is specifically a shortcut
			shouldInitialiseSteam &= !isShortcut;
		} else {
			// only set this app id if one is not already set
			// this correlates to Spacewar, an internal "testing" app id with all features enabled
			useSpacewar = true;
		}

		if (shouldInitialiseSteam) {
			SteamApi steam = useSpacewar ? new SteamApiFfm(480) : new SteamApiFfm();

			var errorMessage = new String[1];
			int res = steam.SteamAPI_InitFlat(errorMessage);
			if (res == SteamApi.k_ESteamAPIInitResult_OK) {
				CUtil.LOGGER.log("Successfully initialised Steam API");
			} else {
				CUtil.LOGGER.error("Failed to initialise Steam API: {} / {}", res, errorMessage[0]);
			}
		}
	}
}
