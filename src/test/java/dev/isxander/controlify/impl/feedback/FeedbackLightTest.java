/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.api.feedback.FeedbackEffect;
import dev.isxander.controlify.impl.feedback.FeedbackTestSupport.TestServices;
import dev.isxander.controlify.impl.feedback.data.ColorGradient;
import dev.isxander.controlify.impl.feedback.data.DataFeedbackEffect;
import dev.isxander.controlify.impl.utils.ColorSpaces;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.FRAME;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.event;
import static dev.isxander.controlify.impl.feedback.FeedbackTestSupport.id;
import static org.junit.jupiter.api.Assertions.assertEquals;

/// Light tracks: gradients, compositing layers over each other, and rendering to the LED.
class FeedbackLightTest {
	private static final float EPSILON = 1e-3f;

	private static ColorGradient gradient(String json) {
		return ColorGradient.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
	}

	private static float[] sample(ColorGradient gradient, float t) {
		float[] rgba = new float[4];
		gradient.sample(t, rgba);
		return rgba;
	}

	private static void assertRgba(float[] expected, float[] actual) {
		for (int i = 0; i < 4; i++) {
			assertEquals(expected[i], actual[i], EPSILON, "component " + i);
		}
	}

	// --- gradients ---


	@Test
	void aTransparentStopDoesNotTintItsNeighbour() {
		ColorGradient fromClearWhite = gradient("[[\"#ffffff00\", 0], [\"#ff0000\", 1]]");

		assertRgba(new float[] { 1f, 0f, 0f, 0.5f }, sample(fromClearWhite, 0.5f));
	}

	@Test
	void stopsAtTheSamePositionStep() {
		ColorGradient step = gradient("[[\"#00ff00\", 0], [\"#00ff00\", 0.5], [\"#ff0000\", 0.5]]");

		assertRgba(new float[] { 0f, 1f, 0f, 1f }, sample(step, 0.49f));
		assertRgba(new float[] { 1f, 0f, 0f, 1f }, sample(step, 0.5f));
	}


	// --- compositing ---

	@Test
	void layersCoverWhatIsBeneathThem() {
		MixedFrame frame = new MixedFrame();
		frame.reset(false);
		frame.light(0x0000ff, 1f);
		frame.light(0xff0000, 0.5f);

		// a crossfade, not the magenta adding would give
		assertEquals(0.5f, frame.lightRed(), EPSILON);
		assertEquals(0f, frame.lightGreen(), EPSILON);
		assertEquals(0.5f, frame.lightBlue(), EPSILON);
		assertEquals(1f, frame.lightAlpha(), EPSILON);
	}


	@Test
	void strengthMakesALayerMoreTransparentNotDarker() {
		MixedFrame sample = new MixedFrame();
		sample.reset(false);
		sample.light(0xff0000, 1f);
		MixedFrame mixed = new MixedFrame();
		mixed.reset(false);
		mixed.light(0x0000ff, 1f);

		mixed.mix(sample, 0.25f);

		assertEquals(0.25f, mixed.lightRed(), EPSILON);
		assertEquals(0.75f, mixed.lightBlue(), EPSILON);
	}


	// --- draw order ---

	private final TestServices services = new TestServices();
	private static final int BASE = 0x000040;
	private final List<Integer> colors = new ArrayList<>();
	private final FeedbackEngine engine = new FeedbackEngine(this.services, List.of(new LightRenderer(this.colors::add, BASE)));

	private static FeedbackEffect<Object> light(int rgb) {
		return (playback, out) -> {
			if (playback.released()) {
				return false;
			}
			out.light(rgb, 1f);
			return true;
		};
	}

	@Test
	void firedEffectsDrawOverHeldOnes() {
		Identifier water = id("water"), damage = id("damage");
		this.services.effects.put(water, light(0x0000ff));
		this.services.effects.put(damage, light(0xff0000));

		// the fired one starts first, so only the trigger kind can put it on top
		this.engine.fire(event("damage"), null, null);
		this.engine.hold(event("water"), null, null);
		this.engine.update(FRAME, false);

		assertEquals(0xff0000, this.colors.getLast());
	}

	@Test
	void aHigherRankDrawsOverAnyTrigger() {
		Identifier water = id("water"), damage = id("damage");
		this.services.effects.put(water, light(0x0000ff));
		this.services.effects.put(damage, light(0xff0000));
		this.services.ranks.put(water, 1);

		this.engine.fire(event("damage"), null, null);
		this.engine.hold(event("water"), null, null);
		this.engine.update(FRAME, false);

		assertEquals(0x0000ff, this.colors.getLast());
	}

	@Test
	void newerEffectsDrawOverOlderOnes() {
		this.services.effects.put(id("a"), light(0x00ff00));
		this.services.effects.put(id("b"), light(0xff0000));

		this.engine.fire(event("a"), null, null);
		this.engine.fire(event("b"), null, null);
		this.engine.update(FRAME, false);
		assertEquals(0xff0000, this.colors.getLast());

		this.engine.fire(event("a"), null, null);
		this.engine.update(1f, false);
		assertEquals(0x00ff00, this.colors.getLast());
	}

	@Test
	void theLightReturnsToItsDefaultWhenNothingCoversIt() {
		this.services.effects.put(id("a"), light(0xff0000));
		this.engine.hold(event("a"), null, null);
		this.engine.update(FRAME, false);
		assertEquals(0xff0000, this.colors.getLast());

		this.engine.release(event("a"));
		this.engine.update(FRAME, false);

		assertEquals(BASE, this.colors.getLast());
	}


	@Test
	void aFadingLayerFadesTowardsTheDefault() {
		MixedFrame frame = new MixedFrame();
		frame.reset(false);
		frame.light(0xff0000, 0.5f);

		new LightRenderer(this.colors::add, BASE).render(frame, FRAME);

		int expectedBlue = Math.round(ColorSpaces.linearToSrgb(ColorSpaces.srgbToLinear(0x40 / 255f) * 0.5f) * 255f);
		int expectedRed = Math.round(ColorSpaces.linearToSrgb(0.5f) * 255f);
		assertEquals(expectedRed << 16 | expectedBlue, this.colors.getLast());
	}

	// --- data effects ---

	@Test
	void aColorTrackPaintsAtItsStrength() {
		DataFeedbackEffect effect = DataFeedbackEffect.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
				"{\"tracks\": [{\"color\": \"#ff0000\", \"envelope\": [[0, 1], [1, 0]]}]}"
		)).getOrThrow();
		this.services.effects.put(id("flash"), effect);

		this.engine.fire(event("flash"), null, null);
		this.engine.update(0.05f, false);
		this.engine.update(0.05f, false);

		assertEquals(0xff0000, this.colors.get(0));
		// sampled at 0.05 s, where the envelope is 0.95: red at 95% opacity over the default
		int red = Math.round(ColorSpaces.linearToSrgb(0.95f) * 255f);
		int blue = Math.round(ColorSpaces.linearToSrgb(ColorSpaces.srgbToLinear(0x40 / 255f) * 0.05f) * 255f);
		assertEquals(red << 16 | blue, this.colors.get(1));
	}
}
