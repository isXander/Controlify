package dev.isxander.controlify.api.screenop;

import net.minecraft.client.gui.components.events.GuiEventListener;

@FunctionalInterface
public interface ElementBehaviourFactory<T extends GuiEventListener> {
	ElementBehaviour create(T element);
}
