/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.impl.screenop.CompositeElementBehaviour;
import dev.isxander.controlify.impl.screenop.ConditionalElementBehaviour;
import dev.isxander.controlify.impl.screenop.behaviours.element.ButtonBehaviour;
import dev.isxander.controlify.impl.screenop.behaviours.element.CycleBehaviour;
import dev.isxander.controlify.impl.screenop.behaviours.element.LayoutElementSnapBehaviour;
import dev.isxander.controlify.impl.screenop.behaviours.element.SliderBehaviour;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;

/// Reusable controller handling, independent of the element's widget implementation.
/// Factories create a separate stateful behaviour for each registered element.
public final class ElementBehaviours {
	private ElementBehaviours() {
	}

	/// Runs members in order. The last next() continues into the enclosing chain.
	/// Focus notifications reach every member; the first present keyboard policy wins.
	public static ElementBehaviour chain(List<? extends ElementBehaviour> behaviours) {
		return new CompositeElementBehaviour(behaviours);
	}

	public static ElementBehaviour chain(ElementBehaviour... behaviours) {
		return chain(List.of(behaviours));
	}

	/// False skips this behaviour and continues the enclosing chain. The condition
	/// is checked on each call, while focus notifications always reach the behaviour.
	public static ElementBehaviour conditional(BooleanSupplier condition, ElementBehaviour behaviour) {
		return new ConditionalElementBehaviour(condition, behaviour);
	}

	/// Participates while the widget is active and visible, checking both on each
	/// call. Otherwise continues the enclosing chain. Focus notifications always
	/// reach the behaviour so it can reset state when a disabled widget loses focus.
	public static <T extends AbstractWidget> ElementBehaviourFactory<T> enabledWidget(ElementBehaviourFactory<? super T> factory) {
		return widget -> conditional(() -> widget.active && widget.visible, factory.create(widget));
	}

	/// Confines cancellation to this branch, then continues the enclosing chain on
	/// normal return. Exceptions propagate and changing screens still stops input.
	/// Keyboard policies and focus notifications retain their ordinary semantics.
	public static ElementBehaviour branch(ElementBehaviour behaviour) {
		return CompositeElementBehaviour.branch(behaviour);
	}

	/// Activates once per invocation, draining additional press pulses. Plays the
	/// standard click sound and navigation haptic. Wrap with conditional to supply
	/// an element-specific enabled/visible check.
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> button(Consumer<? super T> press) {
		return element -> new ButtonBehaviour(() -> press.accept(element));
	}

	/// Uses the element's own click sound instead of the standard one.
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> button(Consumer<? super T> press, Consumer<? super T> sound) {
		return element -> new ButtonBehaviour(() -> press.accept(element), () -> sound.accept(element));
	}

	/// Press toggles editing; Back exits editing before closing the screen. Primary
	/// horizontal navigation adjusts while editing; secondary horizontal navigation
	/// adjusts directly. Each pulse calls adjust with -1 or +1. Losing focus exits
	/// editing. The behaviour owns its editing state.
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> slider(ObjIntConsumer<? super T> adjust) {
		return element -> new SliderBehaviour(direction -> adjust.accept(element, direction));
	}

	/// Secondary horizontal navigation cycles with -1 or +1; activation cycles
	/// forward. Every pulse is applied, including repeated activation pulses.
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> cycle(ObjIntConsumer<? super T> cycle) {
		return element -> new CycleBehaviour(direction -> cycle.accept(element, direction));
	}

	/// Shares an existing editing state with the element, for example when its
	/// rendering or keyboard handling also depends on that state. Adjustment must
	/// work regardless of editing, since secondary navigation can adjust directly.
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> slider(
			ObjIntConsumer<? super T> adjust,
			Predicate<? super T> isEditing,
			BiConsumer<? super T, Boolean> setEditing
	) {
		return element -> new SliderBehaviour(
				direction -> adjust.accept(element, direction),
				() -> isEditing.test(element),
				editing -> setEditing.accept(element, editing)
		);
	}

	/// Adds a centre snap point using live layout geometry, then continues.
	public static ElementBehaviour snapPoint(LayoutElement element) {
		return new LayoutElementSnapBehaviour(element);
	}
}
