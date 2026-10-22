package dev.isxander.controlify.impl.compatibility.sodium.screenop;

//? if sodium {

import dev.isxander.controlify.api.screenop.ElementBehaviourRegistry;
import dev.isxander.controlify.api.screenop.ScreenopEvents;
import net.caffeinemc.mods.sodium.client.gui.VideoSettingsScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;

public final class SodiumScreenBehaviours {
	private SodiumScreenBehaviours() {
	}

	public static void initialize() {
		ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribe(registry -> {
			registry.registerUnchecked(VideoSettingsScreen.class);
			//? if reeses_sodium_options {
			/*if (dev.isxander.controlify.impl.platform.main.PlatformMainUtil.isModLoaded("reeses-sodium-options")) {
				registry.registerUnchecked(me.flashyreese.mods.reeses_sodium_options.client.gui.SodiumVideoOptionsScreen.class);
			}
			*///?}
		});
		ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribe(event -> {
			registerControl(event.registry(), "CyclingControl$CyclingControlElement");
			registerControl(event.registry(), "SliderControl$SliderControlElement");
			registerControl(event.registry(), "TickBoxControl$TickBoxControlElement");
		});
	}

	private static void registerControl(ElementBehaviourRegistry registry, String name) {
		try {
			var type = Class.forName("net.caffeinemc.mods.sodium.client.gui.options.control." + name)
					.asSubclass(GuiEventListener.class);
			registry.registerUnchecked(type);
		} catch (ClassNotFoundException exception) {
			throw new IllegalStateException("Sodium control class is unavailable: " + name, exception);
		}
	}
}
//?}
