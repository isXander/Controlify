/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.sodium.screenop;

//? if sodium {

import dev.isxander.controlify.api.screenop.ElementBehaviourFactory;
import dev.isxander.controlify.api.screenop.ElementBehaviourRegistry;
import dev.isxander.controlify.api.screenop.ElementBehaviours;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import dev.isxander.controlify.impl.screenop.behaviours.element.ControlValueAccess;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;

public final class SodiumScreenBehaviours {
	private SodiumScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribe(registry -> {
			registry.register(VideoSettingsScreen.class, context -> new SodiumScreenBehaviour(context, (SodiumScreenOperations) context.screen()));
			//? if reeses_sodium_options {
			/*if (dev.isxander.controlify.impl.platform.main.PlatformMainUtil.isModLoaded("reeses-sodium-options")) {
				registry.register(me.flashyreese.mods.reeses_sodium_options.client.gui.SodiumVideoOptionsScreen.class, context -> new SodiumScreenBehaviour(context, (SodiumScreenOperations) context.screen()));
			}
			*///?}
		});
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event -> {
			registerControl(event.registry(), "CyclingControl$CyclingControlElement", ElementBehaviours.cycle(
					(widget, direction) -> ((ControlValueAccess) widget).controlify$adjustValue(direction)
			));
			registerControl(event.registry(), "SliderControl$SliderControlElement", ElementBehaviours.secondarySlider(
					(widget, direction) -> ((ControlValueAccess) widget).controlify$adjustValue(direction)
			));
			registerControl(event.registry(), "TickBoxControl$TickBoxControlElement", ElementBehaviours.cycle(
					(widget, direction) -> ((ControlValueAccess) widget).controlify$adjustValue(direction)
			));
		});
	}

	private static void registerControl(ElementBehaviourRegistry registry, String name, ElementBehaviourFactory<GuiEventListener> factory) {
		try {
			var type = Class.forName("net.caffeinemc.mods.sodium.client.gui.options.control." + name)
					.asSubclass(GuiEventListener.class);
			registry.register(type, factory);
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Sodium control class is unavailable: " + name, exception);
		}
	}
}
//?}
