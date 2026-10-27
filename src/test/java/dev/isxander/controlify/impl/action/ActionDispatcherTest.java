/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.action.activator.ActivatorConfig;
import dev.isxander.controlify.impl.action.activator.ContinuousActivator;
import dev.isxander.controlify.impl.action.activator.HoldLatchActivator;
import dev.isxander.controlify.impl.action.activator.PressPulseActivator;
import dev.isxander.controlify.impl.action.activator.ReleasePulseActivator;
import dev.isxander.controlify.impl.input.pipeline.PhysicalToLogicalStage;
import dev.isxander.controlify.impl.input.pipeline.SensorAccumulatorStage;
import dev.isxander.controlify.impl.input.signal.LogicalSignal;
import dev.isxander.controlify.impl.input.signal.PhysicalInputSignal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionDispatcherTest {
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
		ActionImpl screen = action("screen_press", BLOCKING, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl game = action("game_press", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl gameOther = action("game_other", BASE, EAST, PressPulseActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BLOCKING, BASE));

		this.press(1, SOUTH);
		this.press(1, EAST);

		assertTrue(screen.state().consumeAllPulses());
		assertFalse(game.state().consumeAllPulses());
		assertFalse(gameOther.state().consumeAllPulses());
	}

	@Test
	void overlayShadowsOnlyItsOwnInputs() {
		ActionImpl overlay = action("overlay_press", OVERLAY, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl shadowed = action("base_press", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl passing = action("base_other", BASE, EAST, PressPulseActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(OVERLAY, BASE));

		this.press(1, SOUTH);
		this.press(1, EAST);

		assertTrue(overlay.state().consumeAllPulses());
		assertFalse(shadowed.state().consumeAllPulses());
		assertTrue(passing.state().consumeAllPulses());
	}

	@Test
	void transparentContextSharesInputs() {
		ActionImpl upper = action("upper", TRANSPARENT, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl lower = action("lower", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(TRANSPARENT, BASE));

		this.press(1, SOUTH);

		assertTrue(upper.state().consumeAllPulses());
		assertTrue(lower.state().consumeAllPulses());
	}

	@Test
	void becomingUnreachableMidPressCancelsInsteadOfReleasing() {
		ActionImpl hold = action("hold", BASE, SOUTH, HoldLatchActivator.Config.INSTANCE);
		ActionImpl release = action("release", BASE, SOUTH, ReleasePulseActivator.Config.INSTANCE);
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
		ActionImpl release = action("release", BLOCKING, SOUTH, ReleasePulseActivator.Config.INSTANCE);
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
		ActionImpl game = action("game_look", BASE, STICK, ContinuousActivator.Config.INSTANCE);
		ActionImpl screen = action("screen_scroll", BLOCKING, STICK, ContinuousActivator.Config.INSTANCE);
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
		ActionImpl hold = action("hold", BASE, SOUTH, HoldLatchActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BASE));
		this.press(1, SOUTH);

		hold.setBoundInput(EAST);
		this.release(2, SOUTH);
		this.press(3, EAST);

		assertTrue(hold.state().isLatchActive());
	}

	@Test
	void boundChordSuppressesTriggerAndCancelsModifier() {
		ActionImpl chord = action("chord", BASE, LB_A, PressPulseActivator.Config.INSTANCE);
		ActionImpl jump = action("jump", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl aim = action("aim", BASE, LB_SINGLE, HoldLatchActivator.Config.INSTANCE);
		ActionImpl prevSlot = action("prev_slot", BASE, LB_SINGLE, ReleasePulseActivator.Config.INSTANCE);
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
		ActionImpl jump = action("jump", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		ActionImpl aim = action("aim", BASE, LB_SINGLE, HoldLatchActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, A, true);

		assertTrue(jump.state().consumeAllPulses());
		assertTrue(aim.state().isLatchActive());
	}

	@Test
	void mostSpecificBoundChordWins() {
		ActionImpl two = action("two", BASE, LB_RB_A, PressPulseActivator.Config.INSTANCE);
		ActionImpl one = action("one", BASE, RB_A, PressPulseActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, RB, true);
		this.physical(3, A, true);

		assertTrue(two.state().consumeAllPulses());
		assertFalse(one.state().consumeAllPulses());
	}

	@Test
	void chordOnlyClaimsWhereItIsReachable() {
		ActionImpl chord = action("chord", BLOCKING, LB_A, PressPulseActivator.Config.INSTANCE);
		ActionImpl jump = action("jump", BASE, SOUTH, PressPulseActivator.Config.INSTANCE);
		this.dispatcher.setCurrentContexts(List.of(BASE));

		this.physical(1, LB, true);
		this.physical(2, A, true);

		assertFalse(chord.state().consumeAllPulses());
		assertTrue(jump.state().consumeAllPulses());
	}

	private final PhysicalToLogicalStage stage = new PhysicalToLogicalStage(new SensorAccumulatorStage());

	private void physical(long time, Identifier button, boolean pressed) {
		this.stage.onEvent(new PhysicalInputSignal.ButtonChanged(time, button, pressed), this.dispatcher);
	}

	private ActionImpl action(String name, ActionLayer context, LogicalInput input, ActivatorConfig<?> activator) {
		var spec = new ActionSpec(id(name), Component.empty(), Component.empty(), Component.empty(),
				Set.of(context.id()), _ -> true);
		var action = new ActionImpl(spec, input, input, activator, this.dispatcher);
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
