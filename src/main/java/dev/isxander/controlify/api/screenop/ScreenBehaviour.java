/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.api.controller.Controller;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

/// Controller support attached to a screen without modifying the screen's class hierarchy.
/// Instances are created by registered [factories][ScreenBehaviourFactory] and retain
/// state across input calls, widget rebuilds, and reactivation of the same screen.
///
/// Methods taking `next` form independent middleware chains. Call it synchronously
/// at most once to continue that invocation, or omit it to stop the remaining behaviours
/// for that method. It must not be stored for later use. Work after `next.run()`
/// runs after the remaining chain returns. [#onScreenActiveChanged(boolean)] is
/// broadcast instead and cannot be cancelled through a continuation.
///
/// Each controller fixed update runs navigation, synchronizes focus, then runs
/// interaction if the same screen is still current. Cancelling either chain only affects
/// that stage; it does not cancel the other stage or consume any action pulses. Consume
/// or drain pulses explicitly when suppressing another action. Changing screens stops
/// further input dispatch for the previous screen.
public interface ScreenBehaviour {
	/// Handles navigation during a controller fixed update, before interaction.
	/// Use this stage to move focus or adjust values through directional input. With the
	/// built-in routing, focused elements receive navigation from deepest to outermost
	/// before the remaining screen controls.
	///
	/// Omitting `next` stops further navigation only. Interaction still runs on the
	/// same screen using the resulting focus path, even if this method handled navigation.
	///
	/// @param controller the controller whose actions are being processed
	/// @param next continues the navigation chain synchronously, at most once
	default void handleNavigation(Controller controller, Runnable next) {
		next.run();
	}

	/// Handles activation, Back, and other interactions after navigation has completed.
	/// Focus is synchronized between the stages, so built-in element routing targets the
	/// focus path produced by navigation rather than the path at the start of the update.
	///
	/// Omitting `next` prevents remaining interaction behaviours from running for
	/// this invocation. It does not undo navigation, affect later updates, or drain pulses
	/// belonging to actions this behaviour did not consume.
	///
	/// @param controller the controller whose actions are being processed
	/// @param next continues the interaction chain synchronously, at most once
	default void handleInteraction(Controller controller, Runnable next) {
		next.run();
	}

	/// Responds to initialization or rebuilding of the screen's widgets.
	/// The behaviour instance is retained, but widget references may have changed. Resolve
	/// new widgets here, for example when attaching button guides. This hook can also be
	/// called on the first observation of an already initialized screen.
	///
	/// This hook uses continuation propagation; omitting `next` skips later rebuild
	/// handlers. It is separate from activation notifications and controller input.
	///
	/// @param next continues the widget rebuild chain synchronously, at most once
	default void onWidgetRebuild(Runnable next) {
		next.run();
	}

	/// Adds controller-related content to the screen's GUI render state.
	/// This is a rendering hook, separate from fixed input updates and frame updates;
	/// use it for hints, guides, or cursor visuals rather than consuming input pulses.
	/// Content may be extracted before or after calling `next` to control ordering.
	///
	/// @param graphics the GUI render state extractor
	/// @param tickDelta the partial tick supplied by screen rendering
	/// @param controller the controller used for the current screen rendering
	/// @param next continues render state extraction synchronously, at most once
	default void extractRenderState(GuiGraphicsExtractor graphics, float tickDelta, Controller controller, Runnable next) {
		next.run();
	}

	/// Contributes targets for virtual mouse snapping in scaled GUI coordinates.
	/// Call `output.accept(point)` for each target. The remaining chain normally
	/// includes built-in traversal through element behaviours; omitting `next`
	/// replaces those remaining contributions, including descendant targets.
	///
	/// Collection can happen repeatedly as the cursor moves. Read current geometry each
	/// time and do not retain the output consumer beyond this call.
	///
	/// @param output receives this screen's snap points
	/// @param next continues snap point collection synchronously, at most once
	default void collectSnapPoints(Consumer<SnapPoint> output, Runnable next) {
		next.run();
	}

	/// Updates frame-based state before GUI mouse processing.
	/// Unlike navigation and interaction, this runs per frame and can run without a
	/// controller fixed update. Use it for smooth movement or animation rather than assuming
	/// there is a fresh set of action pulses on every call.
	///
	/// @param delta elapsed real time in ticks, not seconds or a partial tick fraction
	/// @param next continues the frame update chain synchronously, at most once
	default void update(float delta, Runnable next) {
		next.run();
	}

	/// Notifies this behaviour when its screen becomes or ceases to be the current screen.
	/// The same context may activate repeatedly, for example when returning from an overlay;
	/// deactivation is not a destruction notification.
	///
	/// This notification is broadcast to every member of a composed chain independently
	/// of cancellation in any other method. [ScreenBehaviours#conditional] forwards it
	/// even when its predicate is false. Release held input or suspend transient state here
	/// rather than relying on a later input callback.
	///
	/// @param active whether this screen is now current
	default void onScreenActiveChanged(boolean active) {
	}
}
