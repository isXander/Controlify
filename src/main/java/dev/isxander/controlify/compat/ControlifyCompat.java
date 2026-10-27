/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat;

import dev.isxander.controlify.compat.fancymenu.FancyMenuCompat;
import dev.isxander.controlify.compat.iris.screenop.IrisScreenBehaviours;
import dev.isxander.controlify.compat.simplevoicechat.SimpleVoiceChatCompat;
import dev.isxander.controlify.compat.sodium.screenop.SodiumScreenBehaviours;
import dev.isxander.controlify.compat.yacl.screenop.YACLScreenBehaviours;
import dev.isxander.controlify.impl.platform.main.PlatformMainUtil;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.util.Util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ControlifyCompat {
	private static final Function<String, Boolean> modsLoaded = Util.memoize(modid ->
			PlatformMainUtil.isModLoaded(modid));
	private static final Set<String> disabledMods = new HashSet<>();

	public static final String IMMEDIATELY_FAST = "immediatelyfast";
	public static final String SIMPLE_VOICE_CHAT = "voicechat";
	public static final String FANCY_MENU = "fancymenu";

	public static void initializeScreenBehaviours() {
		YACLScreenBehaviours.initialize();
		//? if sodium {
		wrapCompatCall("sodium", SodiumScreenBehaviours::initialize);
		//?}
		//? if iris {
		wrapCompatCall("iris", IrisScreenBehaviours::initialize);
		//?}
	}

	public static void init() {
		//? if simple_voice_chat {
		try {
			wrapCompatCall(
					SIMPLE_VOICE_CHAT,
					SimpleVoiceChatCompat::init
			);
		} catch (NoClassDefFoundError e) {
			disabledMods.add(SIMPLE_VOICE_CHAT);
		}
		//?}

		//? if fancy_menu {
		try {
			wrapCompatCall(
					FANCY_MENU,
					FancyMenuCompat::registerActions
			);
		} catch (NoClassDefFoundError e) {
			disabledMods.add(FANCY_MENU);
		}
		//?}
	}

	private static void wrapCompatCall(String modid, Runnable runnable) throws NoClassDefFoundError {
		if (modsLoaded.apply(modid) && !disabledMods.contains(modid)) {
			try {
				runnable.run();
			} catch (Throwable t) {
				CUtil.LOGGER.error("Failed to run compatibility code for {}, potentially unsupported version? Disabling '{}' compat for this instance.", modid, modid, t);
				disabledMods.add(modid);
			}
		}
	}
}
