/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import dev.isxander.controlify.api.feedback.Playback;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaleTest {
	private static Playback<Object> playbackWith(float value) {
		return new Playback<>() {
			@Override public float seconds() { return 0f; }
			@Override public float previousSeconds() { return 0f; }
			@Override public Object params() { return null; }
			@Override public float param(String name, float fallback) { return name.equals("p") ? value : fallback; }
			@Override public boolean held() { return false; }
			@Override public float sinceRelease() { return -1f; }
		};
	}

	@Test
	void linearScaleMapsLinearly() {
		Scale scale = new Scale("p", List.of(0f, 10f), List.of(0f, 1f), Easing.LINEAR);
		assertEquals(0.5f, scale.apply(playbackWith(5f)), 1e-5f);
	}

	@Test
	void easeIsAppliedToTheNormalisedParam() {
		Scale scale = new Scale("p", List.of(0f, 10f), List.of(0.2f, 0.6f), Easing.IN_QUAD);
		// halfway through the range, in_quad gives 0.25
		assertEquals(0.2f + 0.4f * 0.25f, scale.apply(playbackWith(5f)), 1e-5f);
	}

	@Test
	void paramIsClampedToTheFromRange() {
		Scale scale = new Scale("p", List.of(0f, 10f), List.of(0.2f, 0.6f), Easing.IN_QUAD);
		assertEquals(0.2f, scale.apply(playbackWith(-5f)), 1e-5f);
		assertEquals(0.6f, scale.apply(playbackWith(50f)), 1e-5f);
	}

	@Test
	void reversedToRangeStillEases() {
		Scale scale = new Scale("p", List.of(0f, 1f), List.of(1f, 0f), Easing.IN_QUAD);
		assertEquals(0.75f, scale.apply(playbackWith(0.5f)), 1e-5f);
	}
}
