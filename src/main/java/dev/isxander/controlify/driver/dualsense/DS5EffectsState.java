/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver.dualsense;

import org.intellij.lang.annotations.MagicConstant;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

public final class DS5EffectsState {
	public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
		ValueLayout.JAVA_BYTE.withName("ucEnableBits1"),
		ValueLayout.JAVA_BYTE.withName("ucEnableBits2"),

		ValueLayout.JAVA_BYTE.withName("ucRumbleRight"),
		ValueLayout.JAVA_BYTE.withName("ucRumbleLeft"),

		ValueLayout.JAVA_BYTE.withName("ucHeadphoneVolume"),
		ValueLayout.JAVA_BYTE.withName("ucSpeakerVolume"),
		ValueLayout.JAVA_BYTE.withName("ucMicrophoneVolume"),

		ValueLayout.JAVA_BYTE.withName("ucAudioEnableBits"),
		ValueLayout.JAVA_BYTE.withName("ucMicLightMode"),
		ValueLayout.JAVA_BYTE.withName("ucAudioMuteBits"),

		TriggerEffect.LAYOUT.withName("rgucRightTriggerEffect"),
		TriggerEffect.LAYOUT.withName("rgucLeftTriggerEffect"),

		MemoryLayout.sequenceLayout(6, ValueLayout.JAVA_BYTE)
			.withName("unknown1"),

		ValueLayout.JAVA_BYTE.withName("ucEnableBits3"),

		MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_BYTE)
			.withName("unknown2"),

		ValueLayout.JAVA_BYTE.withName("ucLedAnim"),
		ValueLayout.JAVA_BYTE.withName("ucLedBrightness"),
		ValueLayout.JAVA_BYTE.withName("ucPadLights"),

		ValueLayout.JAVA_BYTE.withName("ucLedRed"),
		ValueLayout.JAVA_BYTE.withName("ucLedGreen"),
		ValueLayout.JAVA_BYTE.withName("ucLedBlue")
	);

	public static final int SIZE = Math.toIntExact(LAYOUT.byteSize());

	private static final long ENABLE_BITS_1_OFFSET =
		offsetOf("ucEnableBits1");

	private static final long ENABLE_BITS_2_OFFSET =
		offsetOf("ucEnableBits2");

	private static final long RUMBLE_RIGHT_OFFSET =
		offsetOf("ucRumbleRight");

	private static final long RUMBLE_LEFT_OFFSET =
		offsetOf("ucRumbleLeft");

	private static final long HEADPHONE_VOLUME_OFFSET =
		offsetOf("ucHeadphoneVolume");

	private static final long SPEAKER_VOLUME_OFFSET =
		offsetOf("ucSpeakerVolume");

	private static final long MICROPHONE_VOLUME_OFFSET =
		offsetOf("ucMicrophoneVolume");

	private static final long AUDIO_ENABLE_BITS_OFFSET =
		offsetOf("ucAudioEnableBits");

	private static final long MIC_LIGHT_MODE_OFFSET =
		offsetOf("ucMicLightMode");

	private static final long AUDIO_MUTE_BITS_OFFSET =
		offsetOf("ucAudioMuteBits");

	private static final long RIGHT_TRIGGER_EFFECT_OFFSET =
		offsetOf("rgucRightTriggerEffect");

	private static final long LEFT_TRIGGER_EFFECT_OFFSET =
		offsetOf("rgucLeftTriggerEffect");

	private static final long UNKNOWN_1_OFFSET =
		offsetOf("unknown1");

	private static final long ENABLE_BITS_3_OFFSET =
		offsetOf("ucEnableBits3");

	private static final long UNKNOWN_2_OFFSET =
		offsetOf("unknown2");

	private static final long LED_ANIM_OFFSET =
		offsetOf("ucLedAnim");

	private static final long LED_BRIGHTNESS_OFFSET =
		offsetOf("ucLedBrightness");

	private static final long PAD_LIGHTS_OFFSET =
		offsetOf("ucPadLights");

	private static final long LED_RED_OFFSET =
		offsetOf("ucLedRed");

	private static final long LED_GREEN_OFFSET =
		offsetOf("ucLedGreen");

	private static final long LED_BLUE_OFFSET =
		offsetOf("ucLedBlue");

	static {
		if (SIZE != 47) {
			throw new ExceptionInInitializerError(
				"Expected DS5EffectsState to be 47 bytes, but was " + SIZE
			);
		}

		if (TriggerEffect.SIZE != 11) {
			throw new ExceptionInInitializerError(
				"Expected TriggerEffect to be 11 bytes, but was "
				+ TriggerEffect.SIZE
			);
		}
	}

	@MagicConstant(flagsFromClass = EnableBitFlags1.class)
	public byte ucEnableBits1;

	@MagicConstant(flagsFromClass = EnableBitFlags2.class)
	public byte ucEnableBits2;

	public byte ucRumbleRight;
	public byte ucRumbleLeft;

	public byte ucHeadphoneVolume;
	public byte ucSpeakerVolume;
	public byte ucMicrophoneVolume;

	public byte ucAudioEnableBits;

	@MagicConstant(valuesFromClass = MuteLightState.class)
	public byte ucMicLightMode;

	public byte ucAudioMuteBits;

	public TriggerEffect rgucRightTriggerEffect = TriggerEffect.OFF;
	public TriggerEffect rgucLeftTriggerEffect = TriggerEffect.OFF;

	public final byte[] unknown1 = new byte[6];

	public byte ucEnableBits3;

	public final byte[] unknown2 = new byte[2];

	public byte ucLedAnim;
	public byte ucLedBrightness;
	public byte ucPadLights;

	public byte ucLedRed;
	public byte ucLedGreen;
	public byte ucLedBlue;

	public void writeTo(MemorySegment destination) {
		Objects.requireNonNull(destination, "destination");

		if (destination.byteSize() < SIZE) {
			throw new IllegalArgumentException(
				"Destination segment is "
				+ destination.byteSize()
				+ " bytes, but at least "
				+ SIZE
				+ " are required"
			);
		}

		MemorySegment structureSegment = destination.asSlice(0, SIZE);

		structureSegment.fill((byte) 0);

		structureSegment.set(ValueLayout.JAVA_BYTE, ENABLE_BITS_1_OFFSET, ucEnableBits1);
		structureSegment.set(ValueLayout.JAVA_BYTE, ENABLE_BITS_2_OFFSET, ucEnableBits2);
		structureSegment.set(ValueLayout.JAVA_BYTE, RUMBLE_RIGHT_OFFSET, ucRumbleRight);
		structureSegment.set(ValueLayout.JAVA_BYTE, RUMBLE_LEFT_OFFSET, ucRumbleLeft);
		structureSegment.set(ValueLayout.JAVA_BYTE, HEADPHONE_VOLUME_OFFSET, ucHeadphoneVolume);
		structureSegment.set(ValueLayout.JAVA_BYTE, SPEAKER_VOLUME_OFFSET, ucSpeakerVolume);
		structureSegment.set(ValueLayout.JAVA_BYTE, MICROPHONE_VOLUME_OFFSET, ucMicrophoneVolume);
		structureSegment.set(ValueLayout.JAVA_BYTE, AUDIO_ENABLE_BITS_OFFSET, ucAudioEnableBits);
		structureSegment.set(ValueLayout.JAVA_BYTE, MIC_LIGHT_MODE_OFFSET, ucMicLightMode);
		structureSegment.set(ValueLayout.JAVA_BYTE, AUDIO_MUTE_BITS_OFFSET, ucAudioMuteBits);
		rgucRightTriggerEffect.writeTo(
			structureSegment.asSlice(RIGHT_TRIGGER_EFFECT_OFFSET, TriggerEffect.SIZE)
		);
		rgucLeftTriggerEffect.writeTo(
			structureSegment.asSlice(LEFT_TRIGGER_EFFECT_OFFSET, TriggerEffect.SIZE)
		);
		structureSegment.asSlice(UNKNOWN_1_OFFSET, unknown1.length)
			.copyFrom(MemorySegment.ofArray(unknown1));
		structureSegment.set(ValueLayout.JAVA_BYTE, ENABLE_BITS_3_OFFSET, ucEnableBits3);
		structureSegment.asSlice(UNKNOWN_2_OFFSET, unknown2.length)
			.copyFrom(MemorySegment.ofArray(unknown2));
		structureSegment.set(ValueLayout.JAVA_BYTE, LED_ANIM_OFFSET, ucLedAnim);
		structureSegment.set(ValueLayout.JAVA_BYTE, LED_BRIGHTNESS_OFFSET, ucLedBrightness);
		structureSegment.set(ValueLayout.JAVA_BYTE, PAD_LIGHTS_OFFSET, ucPadLights);
		structureSegment.set(ValueLayout.JAVA_BYTE, LED_RED_OFFSET, ucLedRed);
		structureSegment.set(ValueLayout.JAVA_BYTE, LED_GREEN_OFFSET, ucLedGreen);
		structureSegment.set(ValueLayout.JAVA_BYTE, LED_BLUE_OFFSET, ucLedBlue);
	}

	private static long offsetOf(String fieldName) {
		return LAYOUT.byteOffset(groupElement(fieldName));
	}

	public static final class TriggerEffect {
		public static final TriggerEffect OFF = new TriggerEffect(DualsenseTriggerEffectTypes.OFF, new byte[0]);

		public static final int PARAMETER_COUNT = 10;

		public static final MemoryLayout LAYOUT =
			MemoryLayout.structLayout(
				ValueLayout.JAVA_BYTE.withName("effectType"),
				MemoryLayout.sequenceLayout(
					PARAMETER_COUNT,
					ValueLayout.JAVA_BYTE
				).withName("parameters")
			);

		public static final int SIZE =
			Math.toIntExact(LAYOUT.byteSize());

		private static final long EFFECT_TYPE_OFFSET =
			LAYOUT.byteOffset(groupElement("effectType"));

		private static final long PARAMETERS_OFFSET =
			LAYOUT.byteOffset(groupElement("parameters"));

		public byte effectType;
		public final byte[] parameters = new byte[PARAMETER_COUNT];

		public TriggerEffect() {
		}

		public TriggerEffect(byte effectType, byte[] parameters) {
			Objects.requireNonNull(parameters, "parameters");

			this.effectType = effectType;

			System.arraycopy(
				parameters,
				0,
				this.parameters,
				0,
				Math.min(parameters.length, PARAMETER_COUNT)
			);
		}

		public void writeTo(MemorySegment destination) {
			Objects.requireNonNull(destination, "destination");

			if (destination.byteSize() < SIZE) {
				throw new IllegalArgumentException(
					"Trigger effect destination is "
					+ destination.byteSize()
					+ " bytes, but at least "
					+ SIZE
					+ " are required"
				);
			}

			MemorySegment triggerSegment = destination.asSlice(0, SIZE);

			triggerSegment.fill((byte) 0);

			triggerSegment.set(ValueLayout.JAVA_BYTE, EFFECT_TYPE_OFFSET, effectType);
			triggerSegment.asSlice(PARAMETERS_OFFSET, PARAMETER_COUNT)
				.copyFrom(MemorySegment.ofArray(parameters));
		}
	}

	public static final class EnableBitFlags1 {
		public static final byte ENABLE_RUMBLE_EMULATION = 1;
		public static final byte USE_RUMBLE_NOT_HAPTICS = 1 << 1;
		public static final byte ALLOW_RIGHT_TRIGGER_FFB = 1 << 2;
		public static final byte ALLOW_LEFT_TRIGGER_FFB = 1 << 3;
		public static final byte ALLOW_HEADPHONE_VOLUME = 1 << 4;
		public static final byte ALLOW_SPEAKER_VOLUME = 1 << 5;
		public static final byte ALLOW_MIC_VOLUME = 1 << 6;
		public static final byte ALLOW_AUDIO_CONTROL = (byte) (1 << 7);

		private EnableBitFlags1() {
		}
	}

	public static final class EnableBitFlags2 {
		public static final byte ALLOW_MUTE_LIGHT = 1;
		public static final byte ALLOW_AUDIO_MUTE = 1 << 1;
		public static final byte ALLOW_LED_COLOUR = 1 << 2;
		public static final byte RESET_LIGHTS = 1 << 3;
		public static final byte ALLOW_PLAYER_INDICATORS = 1 << 4;
		public static final byte ALLOW_HAPTIC_LOW_PASS = 1 << 5;
		public static final byte ALLOW_MOTOR_POWER_LEVEL = 1 << 6;
		public static final byte ALLOW_AUDIO_CONTROL_2 = (byte) (1 << 7);

		private EnableBitFlags2() {
		}
	}

	public static final class MuteLightState {
		public static final byte OFF = 0;
		public static final byte ON = 1;
		public static final byte BREATHING = 2;

		@MagicConstant(valuesFromClass = MuteLightState.class)
		public static byte fromBoolean(boolean state) {
			return state ? ON : OFF;
		}

		private MuteLightState() {
		}
	}
}
