package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.screens.Screen;

public interface ScreenBehaviourRegistry {
	/// Adds a screen behaviour factory. Higher priorities run first, then more specific
	/// classes, then newest registrations. The default priority is zero; positive and
	/// negative priorities are both supported. Factories run once per screen context,
	/// after construction.
	<T extends Screen> void register(Class<T> screenClass, int priority, ScreenBehaviourFactory<? super T> factory);

	default <T extends Screen> void register(Class<T> screenClass, ScreenBehaviourFactory<? super T> factory) {
		register(screenClass, 0, factory);
	}

	/// Registers a screen behaviour factory provided by the screen itself.
	<T extends Screen & ScreenBehaviourFactory<? super T>> void register(Class<T> screenClass, int priority);

	default <T extends Screen & ScreenBehaviourFactory<? super T>> void register(Class<T> screenClass) {
		register(screenClass, 0);
	}

	/// Registers a screen behaviour factory provided by the screen itself.
	/// The given screen must implement {@link ScreenBehaviourFactory} but this is not
	/// in the type bound to allow the factory to be implemented by mixins.
	<T extends Screen> void registerUnchecked(Class<T> screenClass, int priority);

	default <T extends Screen> void registerUnchecked(Class<T> screenClass) {
		registerUnchecked(screenClass, 0);
	}
}
