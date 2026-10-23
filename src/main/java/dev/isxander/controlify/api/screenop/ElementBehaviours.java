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

/// Composition and reusable controller handling for GUI elements.
/// Register the returned factories with [ElementBehaviourRegistry] to create independent
/// state for each element in each screen. The button, slider, and cycle helpers use
/// callbacks so custom controls can share the input semantics of vanilla controls.
///
/// The input helpers participate in focus-based control and pass through while the
/// virtual mouse is enabled. They do not check arbitrary element availability; use
/// [#conditional] or [#enabledWidget] when an enabled or visible check is needed.
public final class ElementBehaviours {
	private ElementBehaviours() {
	}

	/// Composes a fixed ordered group without assigning priorities to individual members.
	/// Each member's `next` invokes the following member; the last `next` resumes the
	/// enclosing chain. Omitting `next` stops that invocation, and an empty group continues
	/// immediately. The list is copied while its behaviour instances are retained.
	///
	/// Focus notifications are broadcast to every member, independently of cancellation.
	/// Keyboard policy queries use the first non-empty member result. Input propagation
	/// stops if the owning screen ceases to be current.
	///
	/// @param behaviours the members in invocation order
	/// @return the composed element behaviour
	public static ElementBehaviour chain(List<? extends ElementBehaviour> behaviours) {
		return new CompositeElementBehaviour(behaviours);
	}

	/// Composes behaviours in argument order, with the last `next` resuming the enclosing
	/// chain. Omitting `next` stops that invocation; no arguments produces a pass-through
	/// behaviour. Focus notifications reach every member and the first non-empty keyboard
	/// policy wins. Input propagation stops when the owning screen is no longer current.
	///
	/// This is the varargs equivalent of [#chain(List)] and retains the supplied instances.
	///
	/// @param behaviours the members in invocation order
	/// @return the composed element behaviour
	public static ElementBehaviour chain(ElementBehaviour... behaviours) {
		return chain(List.of(behaviours));
	}

	/// Wraps a retained behaviour with a live participation condition.
	/// The condition is checked separately for navigation, interaction, snap point collection,
	/// and keyboard policy queries. A false condition continues the enclosing chain for
	/// continuation-based callbacks and returns an empty keyboard policy.
	///
	/// [ElementBehaviour#onFocusChanged] always reaches the member, even while disabled,
	/// so transient state such as slider editing can be reset. This skips the wrapped member;
	/// it does not cancel other registered behaviours or limit checks to interaction alone.
	///
	/// @param condition whether the member should participate in this callback
	/// @param behaviour the stateful behaviour to retain
	/// @return the conditional behaviour
	public static ElementBehaviour conditional(BooleanSupplier condition, ElementBehaviour behaviour) {
		return new ConditionalElementBehaviour(condition, behaviour);
	}

	/// Creates behaviours that participate while their widgets are both active and visible.
	/// The factory runs once per element resolution; availability is re-evaluated on every
	/// callback through [#conditional]. Unavailable widgets pass through navigation,
	/// interaction, and snap point collection, and supply no keyboard policy.
	///
	/// Focus notifications always reach the wrapped behaviour, including when the widget
	/// has become inactive or invisible. This wrapper does not suppress other behaviours
	/// registered for the widget.
	///
	/// @param <T> the widget type
	/// @param factory constructs the behaviour to guard for each widget
	/// @return a factory applying the live active-and-visible condition
	public static <T extends AbstractWidget> ElementBehaviourFactory<T> enabledWidget(ElementBehaviourFactory<? super T> factory) {
		return widget -> conditional(() -> widget.active && widget.visible, factory.create(widget));
	}

	/// Confines continuation cancellation to an inner behaviour or composed group.
	/// The enclosing chain resumes after the inner callback returns normally, even if the
	/// inner behaviour omitted `next`. Work after an inner continuation finishes before
	/// the enclosing chain resumes.
	///
	/// Exceptions propagate, and changing screens still stops input propagation. Focus
	/// notifications and first-present keyboard policy selection retain their ordinary
	/// semantics. Consumed action pulses are not restored by the branch.
	///
	/// @param behaviour the member or group whose cancellation is local
	/// @return the branch behaviour
	public static ElementBehaviour branch(ElementBehaviour behaviour) {
		return CompositeElementBehaviour.branch(behaviour);
	}

	/// Creates button-like activation handling with the standard GUI click sound.
	/// During focus-based interaction, a pending GUI press drains all accumulated press
	/// pulses and invokes `press` once. Activation plays the click sound and navigation
	/// haptic, then stops interaction propagation. With no press, or while the virtual mouse
	/// is enabled, the behaviour continues.
	///
	/// This helper performs no availability check; wrap it with [#enabledWidget] or
	/// [#conditional] as appropriate.
	///
	/// @param <T> the element type
	/// @param press activates the element
	/// @return a factory creating button handling for each element
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> button(Consumer<? super T> press) {
		return element -> new ButtonBehaviour(() -> press.accept(element));
	}

	/// Creates button-like activation handling using an element-specific click sound.
	/// During focus-based interaction, a pending GUI press drains all accumulated press
	/// pulses, invokes `sound`, plays the navigation haptic, and invokes `press` once.
	/// Handled activation stops interaction propagation. Without a press, or while the
	/// virtual mouse is enabled, the behaviour continues.
	///
	/// The supplied sound callback replaces the standard click sound. Availability is not
	/// checked by this helper; wrap the factory when needed.
	///
	/// @param <T> the element type
	/// @param press activates the element
	/// @param sound plays this element's activation sound
	/// @return a factory creating button handling for each element
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> button(Consumer<? super T> press, Consumer<? super T> sound) {
		return element -> new ButtonBehaviour(() -> press.accept(element), () -> sound.accept(element));
	}

	/// Creates slider handling with editing state owned by the behaviour.
	/// GUI press toggles editing during interaction. While editing, Back exits editing and
	/// drains a simultaneous press so it cannot immediately reopen editing or close the
	/// screen. Focus changes reset editing.
	///
	/// During navigation, primary horizontal input adjusts while editing; secondary
	/// horizontal input adjusts directly regardless of editing. Each pulse invokes `adjust`
	/// with -1 or +1 and plays navigation haptics. Handled input stops its stage's propagation;
	/// unhandled input continues. The helper passes through while the virtual mouse is enabled.
	/// Since navigation precedes interaction, a press changes editing for subsequent
	/// navigation calls, not for navigation already processed in that fixed update.
	///
	/// @param <T> the element type
	/// @param adjust changes the value by one logical step in the supplied direction
	/// @return a factory creating independently stateful slider handling for each element
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> slider(ObjIntConsumer<? super T> adjust) {
		return element -> new SliderBehaviour(direction -> adjust.accept(element, direction));
	}

	/// Creates discrete option handling for secondary horizontal navigation and activation.
	/// Secondary left/right pulses call `cycle` with -1/+1 during navigation; GUI press
	/// pulses call it with +1 during interaction. Every pulse is applied separately, including
	/// repeated presses, with a navigation haptic for each step.
	///
	/// Handled input stops propagation for that stage; otherwise the behaviour continues.
	/// The helper passes through while the virtual mouse is enabled and does not check
	/// element availability itself.
	///
	/// @param <T> the element type
	/// @param cycle changes the option by one logical step in the supplied direction
	/// @return a factory creating cycle handling for each element
	public static <T extends GuiEventListener> ElementBehaviourFactory<T> cycle(ObjIntConsumer<? super T> cycle) {
		return element -> new CycleBehaviour(direction -> cycle.accept(element, direction));
	}

	/// Creates slider handling that shares the element's existing editing state.
	/// GUI press toggles the state read by `isEditing` through `setEditing`; Back exits
	/// editing and drains a simultaneous press. Every focus change writes `false` so editing
	/// is reset even if the element became unavailable.
	///
	/// Primary horizontal navigation adjusts only while editing; secondary horizontal
	/// navigation adjusts directly in either state. Every pulse invokes `adjust` with -1
	/// or +1 and plays navigation haptics. The adjustment callback must work even when
	/// `isEditing` is false. Handled input stops that stage; unhandled input continues,
	/// and all input passes through while the virtual mouse is enabled.
	///
	/// Navigation runs before interaction, so editing changes from a press affect subsequent
	/// navigation calls. Use this overload when rendering or keyboard handling also depends
	/// on the element's editing state.
	///
	/// @param <T> the element type
	/// @param adjust changes the value by one logical step without requiring editing mode
	/// @param isEditing reads the element's current editing state
	/// @param setEditing updates or resets the element's editing state
	/// @return a factory creating slider handling backed by the element's state
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

	/// Contributes a centre snap point using the element's current layout geometry, then
	/// continues the snap point chain. Coordinates and capture range use scaled GUI pixels.
	/// The element need only implement `LayoutElement`; it need not be a vanilla widget.
	///
	/// This helper performs no active or visible check. Apply eligibility rules through
	/// another behaviour or a conditional wrapper where appropriate.
	///
	/// @param element the layout element whose live bounds determine the snap point
	/// @return the layout-based snap point behaviour
	public static ElementBehaviour snapPoint(LayoutElement element) {
		return new LayoutElementSnapBehaviour(element);
	}
}
