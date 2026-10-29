/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.screenop;

import dev.isxander.controlify.impl.screenop.CompositeScreenBehaviour;
import dev.isxander.controlify.impl.screenop.ConditionalScreenBehaviour;

import java.util.List;
import java.util.function.BooleanSupplier;

/// Composition helpers for stateful [ScreenBehaviour] instances.
/// Create these wrappers once in a [ScreenBehaviourFactory] so member state survives
/// between invocations. Composition controls propagation within each callback;
/// it does not consume action pulses or merge navigation and interaction into one stage.
public final class ScreenBehaviours {
	private ScreenBehaviours() {
	}

	/// Composes a fixed ordered group of behaviours without assigning member priorities.
	/// For callbacks with a continuation, each member's `next` invokes the following member;
	/// the last member's `next` resumes the enclosing chain. Omitting `next` stops that
	/// invocation. An empty group continues immediately. The supplied list is copied.
	///
	/// Activation notifications are broadcast to every member independently of continuations.
	/// Navigation, interaction, and frame update propagation also stop if the current screen
	/// changes. Member state is retained between calls.
	///
	/// @param behaviours the members in invocation order
	/// @return the composed behaviour
	public static ScreenBehaviour chain(List<? extends ScreenBehaviour> behaviours) {
		return new CompositeScreenBehaviour(behaviours);
	}

	/// Composes behaviours in argument order, with the last `next` resuming the enclosing
	/// chain. Omitting a continuation stops that invocation; an empty group continues
	/// immediately. Activation notifications always reach every member. Screen changes
	/// stop further navigation, interaction, and frame update propagation.
	///
	/// This is the varargs equivalent of [#chain(List)] and retains the supplied instances.
	///
	/// @param behaviours the members in invocation order
	/// @return the composed behaviour
	public static ScreenBehaviour chain(ScreenBehaviour... behaviours) {
		return chain(List.of(behaviours));
	}

	/// Wraps a behaviour with a live participation condition.
	/// The predicate is evaluated separately for every continuation-based callback. When
	/// false, the member is skipped and the enclosing chain continues. The member is
	/// retained rather than reconstructed, so its state survives changes to the predicate.
	///
	/// [ScreenBehaviour#onScreenActiveChanged] always reaches the member, even while the
	/// predicate is false. A predicate may select different groups for cursor and focus
	/// modes without those groups needing to inspect one another.
	///
	/// @param condition whether the behaviour should participate in this callback
	/// @param behaviour the stateful member to wrap
	/// @return the conditional behaviour
	public static ScreenBehaviour conditional(BooleanSupplier condition, ScreenBehaviour behaviour) {
		return new ConditionalScreenBehaviour(condition, behaviour);
	}

	/// Confines cancellation to an inner behaviour, usually an explicit [#chain(List)].
	/// The inner callback runs with a terminal continuation; after it returns normally,
	/// the enclosing chain continues even if an inner member omitted `next`.
	/// Work after an inner `next.run()` finishes before the enclosing chain resumes.
	///
	/// Exceptions propagate and do not resume the outer chain. Changing screens still stops
	/// input and frame update propagation. Activation notifications retain their broadcast
	/// semantics. This wrapper does not restore or drain consumed action state.
	///
	/// @param behaviour the member or composed group whose cancellation is local
	/// @return the branch behaviour
	public static ScreenBehaviour branch(ScreenBehaviour behaviour) {
		return CompositeScreenBehaviour.branch(behaviour);
	}
}
