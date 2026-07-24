/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.vmousesnapping;

import java.util.Set;
import java.util.function.Consumer;

/**
 * An interface to implement by gui components to define snap points for virtual mouse snapping.
 * Can also be implemented in a mixin to improve compatibility.
 */
public interface ISnapBehaviour {
	default void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
		for (SnapPoint point : getSnapPoints()) {
			consumer.accept(point);
		}
	}

	@Deprecated
	default Set<SnapPoint> getSnapPoints() {
		return Set.of();
	}
}
