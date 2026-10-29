/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.activator.ContinuousLeafConfig;
import dev.isxander.controlify.impl.action.activator.PulseLeafConfig;
import dev.isxander.controlify.impl.action.activator.SpanLeafConfig;
import dev.isxander.controlify.impl.input.pipeline.PhysicalToLogicalStage;
import dev.isxander.controlify.impl.input.pipeline.SensorAccumulatorStage;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionDispatcherTest {
	private static final PulseLeafConfig RELEASE_PULSE = new PulseLeafConfig(Gesture.RELEASE, false);
	private static final PulseLeafConfig TAP_PULSE = new PulseLeafConfig(Gesture.TAP, false);
	private static final LogicalInput SOUTH = new LogicalInput.Single(id("button/south"));
	private static final LogicalInput EAST = new LogicalInput.Single(id("button/east"));
	private static final Identifier A = id("button/south");
	private static final Identifier LB = id("button/left_shoulder");
	private static final Identifier RB = id("button/right_shoulder");
	private static final LogicalInput LB_SINGLE = new LogicalInput.Single(LB);
	private static final LogicalInput LB_A = new LogicalInput.ModifiedChord(Set.of(LB), A);
	private static final LogicalInput RB_A = new LogicalInput.ModifiedChord(Set.of(RB), A);
	private static final LogicalInput LB_RB_A = new LogicalInput.ModifiedChord(Set.of(LB, RB), A);
	private static final LogicalInput STICK = new LogicalInput.Single(id("axis/right_stick_up"));

	private static final ActionLayer OVERLAY = context("overlay", ActionLayerMode.OVERLAY);
	private static final ActionLayer TRANSPARENT = context("transparent", ActionLayerMode.TRANSPARENT);
	private static final ActionLayer BLOCKING = context("blocking", ActionLayerMode.BLOCKING);
	private static final ActionLayer BASE = context("base", ActionLayerMode.OVERLAY);

	private final ActionDispatcher dispatcher = new ActionDispatcher();

	@Test
	void blockingContextHidesEverythingBelow() {
		ActionImpl screen = action("screen_press", BLOCKING, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl game = action("game_press", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl gameOther = action("game_other", BASE, EAST, PulseLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));

		this.press(1, SOUTH);
		this.press(1, EAST);

		assertTrue(screen.state().consumeAllPulses());
		assertFalse(game.state().consumeAllPulses());
		assertFalse(gameOther.state().consumeAllPulses());
	}

	@Test
	void overlayShadowsOnlyItsOwnInputs() {
		ActionImpl overlay = action("overlay_press", OVERLAY, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl shadowed = action("base_press", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl passing = action("base_other", BASE, EAST, PulseLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(OVERLAY, BASE));

		this.press(1, SOUTH);
		this.press(1, EAST);

		assertTrue(overlay.state().consumeAllPulses());
		assertFalse(shadowed.state().consumeAllPulses());
		assertTrue(passing.state().consumeAllPulses());
	}

	@Test
	void transparentContextSharesInputs() {
		ActionImpl upper = action("upper", TRANSPARENT, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl lower = action("lower", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(TRANSPARENT, BASE));

		this.press(1, SOUTH);

		assertTrue(upper.state().consumeAllPulses());
		assertTrue(lower.state().consumeAllPulses());
	}

	@Test
	void becomingUnreachableMidPressCancelsInsteadOfReleasing() {
		ActionImpl hold = action("hold", BASE, SOUTH, SpanLeafConfig.DEFAULT);
		ActionImpl release = action("release", BASE, SOUTH, RELEASE_PULSE);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.press(1, SOUTH);
		assertTrue(hold.state().isLatchActive());

		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));
		this.frame(2);
		assertFalse(hold.state().isLatchActive());

		// Reopening before the release must not let the old press complete.
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.frame(3);
		this.release(4, SOUTH);
		assertFalse(release.state().consumeAllPulses());
	}

	@Test
	void becomingReachableMidPressDoesNotReceiveTheRelease() {
		ActionImpl release = action("release", BLOCKING, SOUTH, RELEASE_PULSE);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.press(1, SOUTH);

		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));
		this.release(2, SOUTH);

		assertFalse(release.state().consumeAllPulses());

		this.press(3, SOUTH);
		this.release(4, SOUTH);
		assertTrue(release.state().consumeAllPulses());
	}

	@Test
	void heldAxisCarriesOverWhenContextChanges() {
		ActionImpl game = action("game_look", BASE, STICK, ContinuousLeafConfig.INSTANCE);
		ActionImpl screen = action("screen_scroll", BLOCKING, STICK, ContinuousLeafConfig.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));
		this.dispatcher.accept(new LogicalSignal.AxisMoved(1, STICK, 0.7f, 0.7f));
		assertEquals(0.7f, screen.state().getContinuous());
		assertEquals(0f, game.state().getContinuous());

		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.frame(2);

		assertEquals(0f, screen.state().getContinuous());
		assertEquals(0.7f, game.state().getContinuous());
	}

	@Test
	void rebindingCancelsTheOldInput() {
		ActionImpl hold = action("hold", BASE, SOUTH, SpanLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.press(1, SOUTH);

		hold.setBindings(List.of(ActionBinding.of(EAST)));
		this.release(2, SOUTH);
		this.press(3, EAST);

		assertTrue(hold.state().isLatchActive());
	}

	@Test
	void boundChordSuppressesTriggerAndCancelsModifier() {
		ActionImpl chord = action("chord", BASE, LB_A, PulseLeafConfig.DEFAULT);
		ActionImpl jump = action("jump", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl aim = action("aim", BASE, LB_SINGLE, SpanLeafConfig.DEFAULT);
		ActionImpl prevSlot = action("prev_slot", BASE, LB_SINGLE, RELEASE_PULSE);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		assertTrue(aim.state().isLatchActive());
		this.physical(2, A, true);

		assertTrue(chord.state().consumeAllPulses());
		assertFalse(jump.state().consumeAllPulses());
		assertFalse(aim.state().isLatchActive());

		this.physical(3, A, false);
		this.physical(4, LB, false);
		assertFalse(prevSlot.state().consumeAllPulses());

		// Claims end with the chord, so the buttons work alone again.
		this.physical(5, A, true);
		assertTrue(jump.state().consumeAllPulses());
	}

	@Test
	void unboundChordLeavesSinglesAlone() {
		ActionImpl jump = action("jump", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		ActionImpl aim = action("aim", BASE, LB_SINGLE, SpanLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, A, true);

		assertTrue(jump.state().consumeAllPulses());
		assertTrue(aim.state().isLatchActive());
	}

	@Test
	void mostSpecificBoundChordWins() {
		ActionImpl two = action("two", BASE, LB_RB_A, PulseLeafConfig.DEFAULT);
		ActionImpl one = action("one", BASE, RB_A, PulseLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, RB, true);
		this.physical(3, A, true);

		assertTrue(two.state().consumeAllPulses());
		assertFalse(one.state().consumeAllPulses());
	}

	@Test
	void chordOnlyClaimsWhereItIsReachable() {
		ActionImpl chord = action("chord", BLOCKING, LB_A, PulseLeafConfig.DEFAULT);
		ActionImpl jump = action("jump", BASE, SOUTH, PulseLeafConfig.DEFAULT);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, A, true);

		assertFalse(chord.state().consumeAllPulses());
		assertTrue(jump.state().consumeAllPulses());
	}

	@Test
	void deferredTapOnlyReachesReachableOwnersOfItsActuation() {
		ActionImpl gameTap = action("game_tap", BASE, SOUTH, TAP_PULSE);
		ActionImpl screenTap = action("screen_tap", BLOCKING, SOUTH, TAP_PULSE);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.physical(0, A, true);
		this.physical(100_000_000L, A, false);

		// A screen opens and the button is pressed again before the tap window closes. The screen
		// never owned the first tap, and the game action that did is no longer reachable.
		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));
		this.physical(150_000_000L, A, true);
		this.physical(400_000_000L, A, false);

		assertFalse(gameTap.state().consumeAllPulses());
		assertFalse(screenTap.state().consumeAllPulses());
	}

	@Test
	void deferredTapFiresAfterTheWindow() {
		ActionImpl tap = action("tap", BASE, SOUTH, TAP_PULSE);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.physical(0, A, true);
		this.physical(100_000_000L, A, false);
		this.stage.onEvent(new PhysicalSignal.Frame(200_000_000L, 1), this.dispatcher);
		assertFalse(tap.state().consumeAllPulses());

		this.stage.onEvent(new PhysicalSignal.Frame(500_000_000L, 1), this.dispatcher);
		assertTrue(tap.state().consumeAllPulses());
	}

	@Test
	void bindingsMergeIntoOneOutput() {
		var spec = new ActionSpec(id("nav"), Component.empty(), Component.empty(), Component.empty(),
				Set.of(BASE.id()), _ -> true);
		List<ActionBinding> bindings = List.of(ActionBinding.of(SOUTH), ActionBinding.of(EAST));
		var span = new ActionImpl(spec, bindings, bindings, SpanLeafConfig.DEFAULT, this.dispatcher);
		span.reset();
		this.dispatcher.addAction(span);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.press(1, SOUTH);
		this.press(2, EAST);
		assertTrue(span.state().consumePulse());
		// The second binding joined a span already in progress.
		assertFalse(span.state().consumePulse());

		this.release(3, SOUTH);
		assertTrue(span.state().isLatchActive());
		this.release(4, EAST);
		assertFalse(span.state().isLatchActive());
	}

	@Test
	void gestureOverrideOnlyAppliesToEditableLeavesOfTheSameKind() {
		var tap = ActionBinding.of(SOUTH).withGesture(java.util.Optional.of(Gesture.TAP));
		var whileHeld = ActionBinding.of(SOUTH).withGesture(java.util.Optional.of(Gesture.WHILE_HELD));

		assertEquals(java.util.Optional.of(Gesture.TAP), new PulseLeafConfig(Gesture.PRESS, true).gestureFor(tap));
		assertEquals(java.util.Optional.of(Gesture.PRESS), new PulseLeafConfig(Gesture.PRESS, false).gestureFor(tap));
		assertEquals(java.util.Optional.of(Gesture.PRESS), new PulseLeafConfig(Gesture.PRESS, true).gestureFor(whileHeld));
	}

	private final PhysicalToLogicalStage stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());

	private void physical(long time, Identifier button, boolean pressed) {
		this.stage.onEvent(new PhysicalInputSignal.ButtonChanged(time, button, pressed), this.dispatcher);
	}

	private ActionImpl action(String name, ActionLayer context, LogicalInput input, ActivatorConfig<?> activator) {
		var spec = new ActionSpec(id(name), Component.empty(), Component.empty(), Component.empty(),
				Set.of(context.id()), _ -> true);
		var action = new ActionImpl(spec, List.of(ActionBinding.of(input)), List.of(ActionBinding.of(input)), activator, this.dispatcher);
		action.reset();
		this.dispatcher.addAction(action);
		return action;
	}

	private void press(long time, LogicalInput input) {
		this.dispatcher.accept(new LogicalSignal.ButtonPress(time, input));
	}

	private void release(long time, LogicalInput input) {
		this.dispatcher.accept(new LogicalSignal.ButtonRelease(time, input));
	}

	private void frame(long time) {
		this.dispatcher.accept(new LogicalSignal.Frame(time, 1));
	}

	private static ActionLayer context(String name, ActionLayerMode mode) {
		return new ActionLayer(id(name), mode, _ -> true);
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}
}
