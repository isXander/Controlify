package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface ScreenBehaviourFactory<T extends Screen> {
	ScreenBehaviour create(ScreenContext<? extends T> screen);
}
