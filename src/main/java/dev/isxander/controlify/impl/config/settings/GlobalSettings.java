/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings;

import com.google.common.collect.Sets;
import dev.isxander.controlify.impl.config.dto.GlobalConfig;
import dev.isxander.controlify.impl.reacharound.ReachAroundMode;
import dev.isxander.controlify.impl.server.ServerPolicies;
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
	public boolean alwaysKeyboardMovement;
	public List<String> analogueMovementWhitelist;
	public final Set<String> seenServers;
	public boolean showSplitscreenAd;
	public int preferredProfile;
	public boolean initialiseSteamApi;

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
		this.alwaysKeyboardMovement = false;
		this.analogueMovementWhitelist = new ArrayList<>();
		this.seenServers = new HashSet<>();
		this.showSplitscreenAd = true;
		this.preferredProfile = 0;
		this.initialiseSteamApi = true;
	}

	public GlobalSettings(
			Set<Class<?>> virtualMouseScreens,
			boolean mixedInput,
			boolean outOfFocusInput,
			ReachAroundMode reachAround,
			boolean allowServerRumble,
			boolean extraUiSounds,
			boolean notifyLowBattery,
			boolean alwaysKeyboardMovement,
			List<String> analogueMovementWhitelist,
			Set<String> seenServers,
			boolean showSplitscreenAd,
			int preferredProfile,
			boolean initialiseSteamApi
	) {
		this.virtualMouseScreens = new HashSet<>(virtualMouseScreens);
		this.mixedInput = mixedInput;
		this.outOfFocusInput = outOfFocusInput;
		this.reachAround = reachAround;
		this.allowServerRumble = allowServerRumble;
		this.extraUiSounds = extraUiSounds;
		this.notifyLowBattery = notifyLowBattery;
		this.alwaysKeyboardMovement = alwaysKeyboardMovement;
		this.analogueMovementWhitelist = new ArrayList<>(analogueMovementWhitelist);
		this.seenServers = new HashSet<>(seenServers);
		this.showSplitscreenAd = showSplitscreenAd;
		this.preferredProfile = Math.max(0, preferredProfile);
		this.initialiseSteamApi = initialiseSteamApi;
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
			case UNSET -> !server.isRealm() && analogueMovementWhitelist.stream().noneMatch(server.ip::endsWith);
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
				dto.alwaysAllowKeyboardMovement(),
				List.copyOf(dto.analogueMovementWhitelist()),
				Set.copyOf(dto.seenServers()),
				dto.showSplitscreenAd(),
				dto.preferredProfile(),
				dto.initialiseSteamApi()
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
				alwaysKeyboardMovement,
				List.copyOf(analogueMovementWhitelist),
				List.copyOf(seenServers),
				showSplitscreenAd,
				preferredProfile,
				initialiseSteamApi
		);
	}
}
