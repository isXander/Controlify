/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import net.minecraft.util.ExtraCodecs;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TriggerEffectCodecs {

	private TriggerEffectCodecs() {
	}

	public static final MapCodec<DualsenseTriggerEffect.Off> MAP_CODEC_OFF =
		MapCodec.unit(DualsenseTriggerEffect.Off.INSTANCE);

	public static final MapCodec<DualsenseTriggerEffect.Feedback> MAP_CODEC_FEEDBACK =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			CExtraCodecs.byteRange(0, 9).fieldOf("position")
				.forGetter(DualsenseTriggerEffect.Feedback::position),
			CExtraCodecs.byteRange(0, 8).fieldOf("strength")
				.forGetter(DualsenseTriggerEffect.Feedback::strength)
		).apply(instance, DualsenseTriggerEffect.Feedback::new));

	public static final MapCodec<DualsenseTriggerEffect.Weapon> MAP_CODEC_WEAPON =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			CExtraCodecs.byteRange(2, 7).fieldOf("start_position")
				.forGetter(DualsenseTriggerEffect.Weapon::startPosition),
			CExtraCodecs.byteRange(3, 8).fieldOf("end_position")
				.forGetter(DualsenseTriggerEffect.Weapon::endPosition),
			CExtraCodecs.byteRange(0, 8).fieldOf("strength")
				.forGetter(DualsenseTriggerEffect.Weapon::strength)
		).apply(instance, DualsenseTriggerEffect.Weapon::new));

	public static final MapCodec<DualsenseTriggerEffect.Vibration> MAP_CODEC_VIBRATION =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			CExtraCodecs.byteRange(0, 9).fieldOf("position")
				.forGetter(DualsenseTriggerEffect.Vibration::position),
			CExtraCodecs.byteRange(0, 8).fieldOf("amplitude")
				.forGetter(DualsenseTriggerEffect.Vibration::amplitude),
			Codec.BYTE.fieldOf("frequency")
				.forGetter(DualsenseTriggerEffect.Vibration::frequency)
		).apply(instance, DualsenseTriggerEffect.Vibration::new));

	public static final MapCodec<DualsenseTriggerEffect.FeedbackMultiplePosition> MAP_CODEC_FEEDBACK_MULTIPLE_POSITION =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			CExtraCodecs.byteArray(CExtraCodecs.byteRange(0, 9).listOf(10, 10)).fieldOf("strength")
				.forGetter(DualsenseTriggerEffect.FeedbackMultiplePosition::strength)
		).apply(instance, DualsenseTriggerEffect.FeedbackMultiplePosition::new));

	public static final MapCodec<DualsenseTriggerEffect.FeedbackSlope> MAP_CODEC_FEEDBACK_SLOPE =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			CExtraCodecs.byteRange(0, 8).fieldOf("start_position")
				.forGetter(DualsenseTriggerEffect.FeedbackSlope::startPosition),
			CExtraCodecs.byteRange(1, 9).fieldOf("end_position")
				.forGetter(DualsenseTriggerEffect.FeedbackSlope::endPosition),
			CExtraCodecs.byteRange(1, 8).fieldOf("start_strength")
				.forGetter(DualsenseTriggerEffect.FeedbackSlope::startStrength),
			CExtraCodecs.byteRange(1, 8).fieldOf("end_strength")
				.forGetter(DualsenseTriggerEffect.FeedbackSlope::endStrength)
		).apply(instance, DualsenseTriggerEffect.FeedbackSlope::new));

	public static final MapCodec<DualsenseTriggerEffect.VibrationMultiplePosition> MAP_CODEC_VIBRATION_MULTIPLE_POSITION =
		RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BYTE.fieldOf("frequency")
				.forGetter(DualsenseTriggerEffect.VibrationMultiplePosition::frequency),
			CExtraCodecs.byteArray(CExtraCodecs.byteRange(0, 8).listOf(10, 10)).fieldOf("amplitude")
				.forGetter(DualsenseTriggerEffect.VibrationMultiplePosition::amplitude)
		).apply(instance, DualsenseTriggerEffect.VibrationMultiplePosition::new));

	private record TriggerEffectType<T extends DualsenseTriggerEffect>(String id, MapCodec<T> mapCodec) {
	}

	private static final TriggerEffectType<DualsenseTriggerEffect.Off> TYPE_OFF =
		new TriggerEffectType<>("off", MAP_CODEC_OFF);

	private static final TriggerEffectType<DualsenseTriggerEffect.Feedback> TYPE_FEEDBACK =
		new TriggerEffectType<>("feedback", MAP_CODEC_FEEDBACK);

	private static final TriggerEffectType<DualsenseTriggerEffect.Weapon> TYPE_WEAPON =
		new TriggerEffectType<>("weapon", MAP_CODEC_WEAPON);

	private static final TriggerEffectType<DualsenseTriggerEffect.Vibration> TYPE_VIBRATION =
		new TriggerEffectType<>("vibration", MAP_CODEC_VIBRATION);

	private static final TriggerEffectType<DualsenseTriggerEffect.FeedbackMultiplePosition> TYPE_FEEDBACK_MULTIPLE_POSITION =
		new TriggerEffectType<>("feedback_multiple_position", MAP_CODEC_FEEDBACK_MULTIPLE_POSITION);

	private static final TriggerEffectType<DualsenseTriggerEffect.FeedbackSlope> TYPE_FEEDBACK_SLOPE =
		new TriggerEffectType<>("feedback_slope", MAP_CODEC_FEEDBACK_SLOPE);

	private static final TriggerEffectType<DualsenseTriggerEffect.VibrationMultiplePosition> TYPE_VIBRATION_MULTIPLE_POSITION =
		new TriggerEffectType<>("vibration_multiple_position", MAP_CODEC_VIBRATION_MULTIPLE_POSITION);

	private static final Map<String, TriggerEffectType<? extends DualsenseTriggerEffect>> TYPES_BY_ID = Stream.of(
		TYPE_OFF,
		TYPE_FEEDBACK,
		TYPE_WEAPON,
		TYPE_VIBRATION,
		TYPE_FEEDBACK_MULTIPLE_POSITION,
		TYPE_FEEDBACK_SLOPE,
		TYPE_VIBRATION_MULTIPLE_POSITION
	).collect(Collectors.toUnmodifiableMap(
		TriggerEffectType::id,
		Function.identity()
	));

	private static final Codec<TriggerEffectType<? extends DualsenseTriggerEffect>> TYPE_CODEC =
		ExtraCodecs.idResolverCodec(Codec.STRING, TYPES_BY_ID::get, TriggerEffectType::id);

	public static final Codec<DualsenseTriggerEffect> CODEC = TYPE_CODEC.dispatch(
		"type",
		effect -> switch (effect) {
			case DualsenseTriggerEffect.Off _ -> TYPE_OFF;
			case DualsenseTriggerEffect.Feedback _ -> TYPE_FEEDBACK;
			case DualsenseTriggerEffect.Weapon _ -> TYPE_WEAPON;
			case DualsenseTriggerEffect.Vibration _ -> TYPE_VIBRATION;
			case DualsenseTriggerEffect.FeedbackMultiplePosition _ -> TYPE_FEEDBACK_MULTIPLE_POSITION;
			case DualsenseTriggerEffect.FeedbackSlope _ -> TYPE_FEEDBACK_SLOPE;
			case DualsenseTriggerEffect.VibrationMultiplePosition _ -> TYPE_VIBRATION_MULTIPLE_POSITION;
		},
		TriggerEffectType::mapCodec
	);
}
