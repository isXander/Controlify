/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import java.util.function.BooleanSupplier;

/// Renders through one of two renderers for the same hardware, and keeps the other silent,
/// so that a controller whose HD actuators also emulate rumble never plays an effect twice.
public final class SwitchingRenderer implements FeedbackRenderer {
	private final BooleanSupplier usePreferred;
	private final FeedbackRenderer preferred;
	private final FeedbackRenderer fallback;

	public SwitchingRenderer(BooleanSupplier usePreferred, FeedbackRenderer preferred, FeedbackRenderer fallback) {
		this.usePreferred = usePreferred;
		this.preferred = preferred;
		this.fallback = fallback;
	}

	@Override
	public void render(MixedFrame frame, float dt) {
		if (this.usePreferred.getAsBoolean()) {
			this.fallback.silence();
			this.preferred.render(frame, dt);
		} else {
			this.preferred.silence();
			this.fallback.render(frame, dt);
		}
	}

	@Override
	public void silence() {
		this.preferred.silence();
		this.fallback.silence();
	}
}
