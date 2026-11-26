/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.feedback.Band;
import dev.isxander.controlify.api.feedback.FeedbackFrame;
import dev.isxander.controlify.api.feedback.Playback;
import dev.isxander.controlify.api.feedback.Site;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/// One output of a data effect. A track drives exactly one of:
///
/// - `band`: a rumble band (`low` or `high`), which every controller can play
/// - `hz`: a tone at that frequency, for HD controllers
/// - `click`: a single transient, which happens at `delay`
/// - `color`: a layer of the controller's light, colored by a [ColorGradient] at the
///   track's strength
///
/// at a `site` (default `body`; lights ignore it). Its strength is
/// `envelope(t) x level x lfo(t) x scale`, where every part is optional and defaults to 1,
/// and `t` counts from `delay`.
///
/// @param delay    seconds before the track starts; it is silent until then
/// @param requires only play this track on controllers that do, or do not, have HD haptics
public record Track(
		Site site,
		Optional<Band> band,
		Optional<Float> hz,
		Optional<Click> click,
		Optional<ColorGradient> color,
		Optional<Envelope> envelope,
		float level,
		Optional<Lfo> lfo,
		Optional<Scale> scale,
		float delay,
		Optional<Requirement> requires
) {
	private static final Codec<Site> SITE_CODEC = StringRepresentable.fromEnum(Site::values);
	private static final Codec<Band> BAND_CODEC = StringRepresentable.fromEnum(Band::values);

	public static final Codec<Track> CODEC = RecordCodecBuilder.<Track>create(instance -> instance.group(
			SITE_CODEC.optionalFieldOf("site", Site.BODY).forGetter(Track::site),
			BAND_CODEC.optionalFieldOf("band").forGetter(Track::band),
			Codec.FLOAT.optionalFieldOf("hz").forGetter(Track::hz),
			Click.CODEC.optionalFieldOf("click").forGetter(Track::click),
			ColorGradient.CODEC.optionalFieldOf("color").forGetter(Track::color),
			Envelope.CODEC.optionalFieldOf("envelope").forGetter(Track::envelope),
			Codec.FLOAT.optionalFieldOf("level", 1f).forGetter(Track::level),
			Lfo.CODEC.optionalFieldOf("lfo").forGetter(Track::lfo),
			Scale.CODEC.optionalFieldOf("scale").forGetter(Track::scale),
			Codec.FLOAT.optionalFieldOf("delay", 0f).forGetter(Track::delay),
			Requirement.CODEC.optionalFieldOf("requires").forGetter(Track::requires)
	).apply(instance, Track::new)).validate(track -> {
		int targets = (track.band.isPresent() ? 1 : 0) + (track.hz.isPresent() ? 1 : 0)
				+ (track.click.isPresent() ? 1 : 0) + (track.color.isPresent() ? 1 : 0);
		if (targets != 1) {
			return DataResult.error(() -> "A track needs exactly one of 'band', 'hz', 'click' or 'color'");
		}
		if (track.hz.isPresent() && !(track.hz.get() > 0f && Float.isFinite(track.hz.get()))) {
			return DataResult.error(() -> "A track's 'hz' must be positive");
		}
		if (!Float.isFinite(track.delay) || track.delay < 0f) {
			return DataResult.error(() -> "A track's 'delay' cannot be negative");
		}
		return DataResult.success(track);
	});

	/// The time in seconds after which this track no longer changes, or empty if it has no
	/// envelope and so no length of its own.
	public Optional<Float> end() {
		if (this.click.isPresent()) {
			return Optional.of(this.delay);
		}
		return this.envelope.map(envelope -> this.delay + envelope.end());
	}

	/// Writes this track's output for the playback's current time.
	///
	/// @param fade a 0 to 1 factor applied on top, used for release tails
	public void write(Playback<?> playback, FeedbackFrame out, float fade) {
		if (this.requires.isPresent() && this.requires.get().hd != out.hd()) {
			return;
		}

		if (this.click.isPresent()) {
			if (playback.crossed(this.delay)) {
				float intensity = this.level * this.scale.map(scale -> scale.apply(playback)).orElse(1f) * fade;
				out.click(this.site, intensity, this.click.get().sharpness());
			}
			return;
		}

		float local = playback.seconds() - this.delay;
		if (local < 0f) {
			return;
		}
		if (this.color.isPresent()) {
			// strength picks the color; fading and the user's strength make it more transparent
			float[] rgba = new float[4];
			this.color.get().sample(Math.clamp(this.strengthAt(local, playback), 0f, 1f), rgba);
			out.light(rgba[0], rgba[1], rgba[2], rgba[3] * fade);
			return;
		}

		float value = this.strengthAt(local, playback) * fade;
		if (this.band.isPresent()) {
			out.band(this.site, this.band.get(), value);
		} else {
			out.voice(this.site, this.hz.orElseThrow(), value);
		}
	}

	/// @param local seconds since this track started
	float strengthAt(float local, Playback<?> playback) {
		float value = this.level;
		if (this.envelope.isPresent()) {
			// past the last point, the final strength is held
			value *= this.envelope.get().at(local);
		}
		if (this.lfo.isPresent()) {
			value *= this.lfo.get().at(local);
		}
		if (this.scale.isPresent()) {
			value *= this.scale.get().apply(playback);
		}
		return value;
	}

	/// @param sharpness 0 is a dull thud, 1 is a crisp tick
	public record Click(float sharpness) {
		static final Codec<Click> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.floatRange(0f, 1f).optionalFieldOf("sharpness", 0.5f).forGetter(Click::sharpness)
		).apply(instance, Click::new));
	}

	public enum Requirement implements StringRepresentable {
		HD("hd", true),
		NO_HD("no_hd", false);

		static final Codec<Requirement> CODEC = StringRepresentable.fromEnum(Requirement::values);

		private final String name;
		private final boolean hd;

		Requirement(String name, boolean hd) {
			this.name = name;
			this.hd = hd;
		}

		@Override
		public @NonNull String getSerializedName() {
			return this.name;
		}
	}
}
