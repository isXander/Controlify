/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.settings;

import com.google.common.collect.Sets;
import dev.isxander.controlify.config.dto.GlobalConfig;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import dev.isxander.controlify.server.ServerPolicies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GlobalSettings {
	public final Set<Class<?>> virtualMouseScreens;
	public boolean mixedInput;
	public boolean outOfFocusInput;
	public ReachAroundMode reachAround;
	public boolean allowServerRumble;
	public boolean extraUiSounds;
	public boolean notifyLowBattery;
	public float ingameButtonGuideScale;
	public boolean useEnhancedSteamDeckDriver;
	public boolean alwaysKeyboardMovement;
	public List<String> analogueMovementWhitelist;
	public final Set<String> seenServers;
	public boolean showSplitscreenAd;
	public int preferredProfile;

	private GlobalSettings() {
		this.virtualMouseScreens = Sets.newHashSet(
				AbstractContainerScreen.class
		);
		this.mixedInput = false;
		this.outOfFocusInput = false;
		this.reachAround = ReachAroundMode.OFF;
		this.allowServerRumble = true;
		this.extraUiSounds = true;
		this.notifyLowBattery = true;
		this.ingameButtonGuideScale = 1f;
		this.useEnhancedSteamDeckDriver = true;
		this.alwaysKeyboardMovement = false;
		this.analogueMovementWhitelist = new ArrayList<>();
		this.seenServers = new HashSet<>();
		this.showSplitscreenAd = true;
		this.preferredProfile = 0;
	}

	public GlobalSettings(
			Set<Class<?>> virtualMouseScreens,
			boolean mixedInput,
			boolean outOfFocusInput,
			ReachAroundMode reachAround,
			boolean allowServerRumble,
			boolean extraUiSounds,
			boolean notifyLowBattery,
			float ingameButtonGuideScale,
			boolean useEnhancedSteamDeckDriver,
			boolean alwaysKeyboardMovement,
			List<String> analogueMovementWhitelist,
			Set<String> seenServers,
			boolean showSplitscreenAd,
			int preferredProfile
	) {
		this.virtualMouseScreens = new HashSet<>(virtualMouseScreens);
		this.mixedInput = mixedInput;
		this.outOfFocusInput = outOfFocusInput;
		this.reachAround = reachAround;
		this.allowServerRumble = allowServerRumble;
		this.extraUiSounds = extraUiSounds;
		this.notifyLowBattery = notifyLowBattery;
		this.ingameButtonGuideScale = ingameButtonGuideScale;
		this.useEnhancedSteamDeckDriver = useEnhancedSteamDeckDriver;
		this.alwaysKeyboardMovement = alwaysKeyboardMovement;
		this.analogueMovementWhitelist = new ArrayList<>(analogueMovementWhitelist);
		this.seenServers = new HashSet<>(seenServers);
		this.showSplitscreenAd = showSplitscreenAd;
		this.preferredProfile = Math.max(0, preferredProfile);
	}

	public boolean shouldUseKeyboardMovement() {
		if (alwaysKeyboardMovement) {
			return true;
		}

		ServerData server = Minecraft.getInstance().getCurrentServer();
		if (server == null) {
			return false;
		}

		return switch (ServerPolicies.ANALOGUE_MOVEMENT.getPolicy()) {
			case ALLOWED -> false;
			case DISALLOWED -> true;
			case UNSET -> analogueMovementWhitelist.stream().noneMatch(server.ip::endsWith);
		};
	}

	public static GlobalSettings defaults() {
		return new GlobalSettings();
	}

	public static GlobalSettings fromDTO(GlobalConfig dto) {
		return new GlobalSettings(
				dto.virtualMouseScreens()
						.stream()
						.flatMap(className -> {
							try {
								return Stream.of(Class.forName(className));
							} catch (ClassNotFoundException e) {
								return Stream.empty();
							}
						})
						.collect(Collectors.toSet()),
				dto.mixedInput(),
				dto.outOfFocusInput(),
				dto.reachAround(),
				dto.allowServerRumble(),
				dto.extraUiSounds(),
				dto.notifyLowBattery(),
				dto.ingameButtonGuideScale(),
				dto.useEnhancedSteamDeckDriver(),
				dto.alwaysAllowKeyboardMovement(),
				List.copyOf(dto.analogueMovementWhitelist()),
				Set.copyOf(dto.seenServers()),
				dto.showSplitscreenAd(),
				dto.preferredProfile()
		);
	}

	public GlobalConfig toDTO() {
		return new GlobalConfig(
				virtualMouseScreens
						.stream()
						.map(Class::getName)
						.toList(),
				mixedInput,
				outOfFocusInput,
				reachAround,
				allowServerRumble,
				extraUiSounds,
				notifyLowBattery,
				ingameButtonGuideScale,
				useEnhancedSteamDeckDriver,
				alwaysKeyboardMovement,
				List.copyOf(analogueMovementWhitelist),
				List.copyOf(seenServers),
				showSplitscreenAd,
				preferredProfile
		);
	}
}
