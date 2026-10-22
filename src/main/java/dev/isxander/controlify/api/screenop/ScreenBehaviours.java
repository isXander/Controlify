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

/// Helpers for composing stateful screen behaviours. Construct them once in a
/// screen factory so their state survives between invocations.
public final class ScreenBehaviours {
	private ScreenBehaviours() {
	}

	/// Runs members in order. The last next() continues into the enclosing chain.
	/// Activation notifications always reach every member.
	public static ScreenBehaviour chain(List<? extends ScreenBehaviour> behaviours) {
		return new CompositeScreenBehaviour(behaviours);
	}

	public static ScreenBehaviour chain(ScreenBehaviour... behaviours) {
		return chain(List.of(behaviours));
	}

	/// Evaluates the condition on each call. False skips this behaviour and continues
	/// the enclosing chain. Activation notifications still reach the behaviour.
	public static ScreenBehaviour conditional(BooleanSupplier condition, ScreenBehaviour behaviour) {
		return new ConditionalScreenBehaviour(condition, behaviour);
	}

	/// Confines cancellation to the supplied behaviour, usually a chain. Once that
	/// invocation returns normally, the enclosing chain continues even if the branch
	/// omitted next(). Exceptions propagate and changing screens still stops input.
	/// Work after an inner next() finishes before the enclosing chain resumes.
	public static ScreenBehaviour branch(ScreenBehaviour behaviour) {
		return CompositeScreenBehaviour.branch(behaviour);
	}
}
