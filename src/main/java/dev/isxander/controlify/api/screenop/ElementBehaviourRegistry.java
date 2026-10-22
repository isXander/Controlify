package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ElementBehaviourRegistry {
	/// Adds a factory within this screen; registering the same type again composes
	/// another behaviour instead of replacing it. Higher priorities run first, then
	/// more specific types, then newest registrations. Registrations close after the event.
	/// The default priority is zero; positive and negative priorities are both supported.
	<T extends GuiEventListener> void register(Class<T> elementClass, int priority, ElementBehaviourFactory<? super T> factory);

	default <T extends GuiEventListener> void register(Class<T> elementClass, ElementBehaviourFactory<? super T> factory) {
		register(elementClass, 0, factory);
	}

	<T extends GuiEventListener & ElementBehaviourFactory<? super T>> void register(Class<T> elementClass, int priority);

	default <T extends GuiEventListener & ElementBehaviourFactory<? super T>> void register(Class<T> elementClass) {
		register(elementClass, 0);
	}

	<T extends GuiEventListener> void registerUnchecked(Class<T> elementClass, int priority);

	default <T extends GuiEventListener> void registerUnchecked(Class<T> elementClass) {
		registerUnchecked(elementClass, 0);
	}
}
