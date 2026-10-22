/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import java.util.List;
import java.util.function.BiConsumer;

final class BehaviourChain {
	private BehaviourChain() {
	}

	static <B> void run(List<B> behaviours, BiConsumer<B, Runnable> invoke, Runnable terminal) {
		run(behaviours, 0, invoke, terminal);
	}

	private static <B> void run(List<B> behaviours, int index, BiConsumer<B, Runnable> invoke, Runnable terminal) {
		if (index == behaviours.size()) {
			terminal.run();
			return;
		}
		var next = new Continuation(() -> run(behaviours, index + 1, invoke, terminal));
		try {
			invoke.accept(behaviours.get(index), next);
		} finally {
			next.active = false;
		}
	}

	private static final class Continuation implements Runnable {
		private final Runnable operation;
		private boolean active = true;
		private boolean used;

		private Continuation(Runnable operation) {
			this.operation = operation;
		}

		@Override
		public void run() {
			if (!active || used) {
				throw new IllegalStateException("A behaviour continuation must be called synchronously and at most once");
			}
			used = true;
			operation.run();
		}
	}
}
