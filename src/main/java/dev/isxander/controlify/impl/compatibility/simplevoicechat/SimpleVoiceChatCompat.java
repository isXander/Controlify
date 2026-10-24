/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.simplevoicechat;

//? if simple_voice_chat {

import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.client.KeyEvents;
import dev.isxander.controlify.api.action.BuiltinActionContexts;
import dev.isxander.controlify.api.action.Action;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.action.ActionEvents;
import dev.isxander.controlify.api.action.ActionProxyManager;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.ActionSpecRegistry;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.compatibility.simplevoicechat.mixins.KeyEventsAccessor;
import dev.isxander.controlify.impl.platform.client.PlatformClientUtil;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SimpleVoiceChatCompat {
	private static ActionAccessor pttHoldSupplier, pttToggleSupplier;
	private static ActionAccessor whisperHoldSupplier, whisperToggleSupplier;
	private static ActionAccessor muteMicrophoneSupplier;

	private static boolean pttDown, whisperDown;
	private static boolean pttToggleDown, whisperToggleDown;
	private static boolean previousPttHold, previousWhisperHold;
	private static Action previousPttAction, previousWhisperAction;

	public static void init() {
		ActionEvents.REGISTER_ACTIONS.subscribe(SimpleVoiceChatCompat::registerActions);
		ActionEvents.REGISTER_PROXIES.subscribe(SimpleVoiceChatCompat::registerProxies);
		PlatformClientUtil.registerClientTickEnded(SimpleVoiceChatCompat::tick);
	}

	private static void registerActions(ActionSpecRegistry registry) {
		Component category = Component.translatable("key.categories.voicechat");
		pttHoldSupplier = register(registry, category, "ptt_hold", Component.translatable("controlify.action.voicechat.ptt_hold"));
		pttToggleSupplier = register(registry, category, "ptt_toggle", Component.translatable("controlify.action.voicechat.ptt_toggle"));
		whisperHoldSupplier = register(registry, category, "whisper_hold", Component.translatable("key.whisper")
				.append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.hold")));
		whisperToggleSupplier = register(registry, category, "whisper_toggle", Component.translatable("key.whisper")
				.append(CommonComponents.SPACE).append(Component.translatable("controlify.compat.svc.toggle")));
		muteMicrophoneSupplier = register(registry, category, "mute_microphone", Component.translatable("controlify.action.voicechat.mute_microphone"));
	}

	private static ActionAccessor register(ActionSpecRegistry registry, Component category, String path, Component name) {
		return registry.register(ActionSpec.builder()
				.id(Identifier.fromNamespaceAndPath("voicechat", path))
				.name(name)
				.category(category)
				.context(BuiltinActionContexts.IN_GAME)
				.build());
	}

	private static void registerProxies(ActionProxyManager proxies) {
		proxies.dontAutoProxyKeyMapping(KeyEvents.KEY_PTT);
		proxies.dontAutoProxyKeyMapping(KeyEvents.KEY_WHISPER);
		proxies.proxyKeyMapping(muteMicrophoneSupplier, KeyEvents.KEY_MUTE);
	}

	private static void tick(Minecraft minecraft) {
		Controlify controlify = Controlify.instance();
		var controller = controlify.getCurrentController().orElse(null);
		if (controller == null || pttHoldSupplier == null || pttToggleSupplier == null
				|| whisperHoldSupplier == null || whisperToggleSupplier == null) {
			clearState();
			return;
		}

		// Resolve on each tick: profile changes may replace action instances.
		Action pttHold = pttHoldSupplier.onOrNull(controller);
		Action pttToggle = pttToggleSupplier.onOrNull(controller);
		Action whisperHold = whisperHoldSupplier.onOrNull(controller);
		Action whisperToggle = whisperToggleSupplier.onOrNull(controller);
		if (pttHold == null || pttToggle == null || whisperHold == null || whisperToggle == null) {
			clearState();
			return;
		}
		if (!controlify.currentInputMode().isController()
				|| (!minecraft.isWindowActive() && !controlify.config().getSettings().globalSettings().outOfFocusInput)) {
			// Discard ineligible toggle presses so they cannot replay when focus/input returns.
			pttToggle.pulse().consumeAllPulses();
			whisperToggle.pulse().consumeAllPulses();
			clearState();
			return;
		}
		if (pttToggle != previousPttAction || whisperToggle != previousWhisperAction) {
			clearState();
			previousPttAction = pttToggle;
			previousWhisperAction = whisperToggle;
		}

		// Pulses accumulate independently of frame/tick cadence; consume each pulse once.
		while (pttToggle.pulse().consumePulse()) {
			pttToggleDown = !pttToggleDown;
			checkConnected();
		}
		while (whisperToggle.pulse().consumePulse()) {
			whisperToggleDown = !whisperToggleDown;
			checkConnected();
		}
		boolean pttHeld = pttHold.latch().isLatchActive();
		boolean whisperHeld = whisperHold.latch().isLatchActive();
		if ((pttHeld && !previousPttHold) || (whisperHeld && !previousWhisperHold)) {
			checkConnected();
		}
		previousPttHold = pttHeld;
		previousWhisperHold = whisperHeld;
		pttDown = pttHeld || pttToggleDown;
		whisperDown = whisperHeld || whisperToggleDown;

		controller.dualsense().ifPresent(ds ->
				ds.setMuteLight(ClientManager.getPlayerStateManager().isMuted()));
	}

	private static void clearState() {
		pttDown = whisperDown = false;
		pttToggleDown = whisperToggleDown = false;
		previousPttHold = previousWhisperHold = false;
		previousPttAction = previousWhisperAction = null;
	}

	public static boolean isPTTDown() {
		return pttDown;
	}

	public static boolean isWhisperDown() {
		return whisperDown;
	}

	private static void checkConnected() {
		if (MinecraftUtil.getOverlay() == null && MinecraftUtil.getScreen() == null) {
			((KeyEventsAccessor) ClientManager.instance().getKeyEvents()).invokeCheckConnected();
		}
	}

}
//?}
