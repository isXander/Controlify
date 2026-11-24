/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.Site;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MixedFrameTest {
	private static MixedFrame voices(int count, float hz, float amp) {
		MixedFrame sample = new MixedFrame();
		sample.reset(true);
		for (int i = 0; i < count; i++) {
			sample.voice(Site.BODY, hz + i * 10f, amp);
		}
		return sample;
	}

	@Test
	void whenVoicesRunOutTheLowestPriorityPlaybackLosesThemEvenIfLouder() {
		MixedFrame mixed = new MixedFrame();
		mixed.reset(true);

		mixed.mix(voices(MixedFrame.MAX_VOICES, 100f, 1f), 1f);
		mixed.mix(voices(2, 500f, 0.1f), 1f);

		assertEquals(MixedFrame.MAX_VOICES, mixed.voiceCount());
		int quiet = 0;
		for (int i = 0; i < mixed.voiceCount(); i++) {
			if (mixed.voiceAmp(i) < 0.5f) {
				quiet++;
			}
		}
		assertEquals(2, quiet);
	}

	@Test
	void withinOnePlaybackTheQuietestVoicesAreDropped() {
		MixedFrame sample = voices(MixedFrame.MAX_VOICES, 100f, 1f);
		sample.voice(Site.BODY, 900f, 0.1f);

		for (int i = 0; i < sample.voiceCount(); i++) {
			assertEquals(1f, sample.voiceAmp(i));
		}
	}
}
