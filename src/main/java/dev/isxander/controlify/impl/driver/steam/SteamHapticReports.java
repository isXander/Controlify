/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver.steam;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;

/// The haptic reports of Valve controllers, as laid out by the `pack(1)` structs in SDL's
/// `controller_structs.h`. Everything is little-endian with no padding.
///
/// There are two families:
///
/// - [Output]: HID *output* reports, used by the 2026 Steam Controller ("Triton"). The
///   first byte is the report id. SDL forwards these as-is when given to
///   `SDL_SendGamepadEffect`.
/// - [Feature]: HID *feature* reports, the older mechanism. The haptic command and rumble
///   messages here are marked "Deck only" in SDL's sources.
///
/// Each method builds one complete report, ready to send.
public final class SteamHapticReports {
	private static final ValueLayout.OfByte U8 = ValueLayout.JAVA_BYTE;
	private static final ValueLayout.OfShort U16 = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN);

	/// `haptic_type_t`: what a haptic command plays.
	public static final byte TYPE_OFF = 0;
	public static final byte TYPE_TICK = 1;
	public static final byte TYPE_CLICK = 2;
	public static final byte TYPE_TONE = 3;
	public static final byte TYPE_RUMBLE = 4;
	public static final byte TYPE_NOISE = 5;
	public static final byte TYPE_SCRIPT = 6;
	public static final byte TYPE_LOG_SWEEP = 7;

	/// Output reports of the 2026 Steam Controller.
	public static final class Output {
		/// Triton command/tone/sweep/script targets are indices, not Deck side bitmasks.
		/// See https://github.com/iczero/steam-controller-stuff/blob/master/dissector.lua
		/// (`haptic_command_sides` and `haptic_script_sides`). Commands support trackpads only.
		public static final byte TRACKPAD_LEFT = 0;
		public static final byte TRACKPAD_RIGHT = 1;
		public static final byte TRACKPAD_BOTH = 2;
		public static final byte INTERNAL_LEFT = 3;
		public static final byte INTERNAL_RIGHT = 4;
		public static final byte INTERNAL_BOTH = 5;

		/// Pulse reports swap the left/right trackpad indices. Never use the tone targets
		/// for trackpad pulses; the internal actuator indices are unchanged.
		public static final byte PULSE_TRACKPAD_LEFT = 1;
		public static final byte PULSE_TRACKPAD_RIGHT = 0;
		public static final byte PULSE_TRACKPAD_BOTH = 2;

		public static final byte ID_HAPTIC_RUMBLE = (byte) 0x80;
		public static final byte ID_HAPTIC_PULSE = (byte) 0x81;
		public static final byte ID_HAPTIC_COMMAND = (byte) 0x82;
		public static final byte ID_HAPTIC_LFO_TONE = (byte) 0x83;
		public static final byte ID_HAPTIC_LOG_SWEEP = (byte) 0x84;
		public static final byte ID_HAPTIC_SCRIPT = (byte) 0x85;

		/// `MsgHapticRumble`. The controller stops rumbling about 50 ms after the last report.
		public static final StructLayout RUMBLE = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("type"),
				U16.withName("intensity"),
				U16.withName("left_speed"),
				U8.withName("left_gain"),
				U16.withName("right_speed"),
				U8.withName("right_gain")
		);

		/// `MsgHapticPulse`: a train of pulses.
		public static final StructLayout PULSE = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("side"),
				U16.withName("on_us"),
				U16.withName("off_us"),
				U16.withName("repeat_count")
		);

		/// `MsgHapticCommand`: a canned effect such as a tick or a click.
		public static final StructLayout COMMAND = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("side"),
				U8.withName("command"),
				U8.withName("gain_db")
		);

		/// `MsgHapticLfoTone`: a tone, optionally modulated by a low-frequency oscillator.
		public static final StructLayout LFO_TONE = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("side"),
				U8.withName("gain_db"),
				U16.withName("frequency"),
				U16.withName("duration_ms"),
				U16.withName("lfo_freq"),
				U8.withName("lfo_depth")
		);

		/// `MsgHapticLogSweep`: a logarithmic sine sweep between two frequencies.
		public static final StructLayout LOG_SWEEP = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("side"),
				U8.withName("gain_db"),
				U16.withName("duration_ms"),
				U16.withName("start_frequency"),
				U16.withName("end_frequency")
		);

		/// `MsgHapticScript`: a haptic script stored on the controller.
		public static final StructLayout SCRIPT = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("side"),
				U8.withName("script_id"),
				U8.withName("gain_db")
		);

		/// @param leftSpeed  0 to 65535
		/// @param rightSpeed 0 to 65535
		public static byte[] rumble(int leftSpeed, int rightSpeed) {
			Report report = new Report(RUMBLE);
			report.u8("report_id", ID_HAPTIC_RUMBLE);
			report.u16("left_speed", leftSpeed);
			report.u16("right_speed", rightSpeed);
			return report.bytes();
		}

		/// @param onMicros    how long each pulse lasts
		/// @param offMicros   the gap between pulses
		/// @param repeatCount how many pulses
		public static byte[] pulse(byte side, int onMicros, int offMicros, int repeatCount) {
			Report report = new Report(PULSE);
			report.u8("report_id", ID_HAPTIC_PULSE);
			report.u8("side", side);
			report.u16("on_us", onMicros);
			report.u16("off_us", offMicros);
			report.u16("repeat_count", repeatCount);
			return report.bytes();
		}

		/// @param side one of the `TRACKPAD_` constants (commands cannot target internal actuators)
		/// @param command `TYPE_OFF`, `TYPE_TICK` or `TYPE_CLICK`; other Deck types are unsupported
		/// @param gainDb  loudness in decibels; may be positive, the controller limits it
		public static byte[] command(byte side, byte command, int gainDb) {
			Report report = new Report(COMMAND);
			report.u8("report_id", ID_HAPTIC_COMMAND);
			report.u8("side", side);
			report.u8("command", command);
			report.u8("gain_db", (byte) gainDb);
			return report.bytes();
		}

		/// @param frequency    the tone, in hertz
		/// @param lfoFrequency how often the tone pulses, in hertz; 0 for a steady tone
		/// @param lfoDepth     how deep the pulsing is, as a percentage
		public static byte[] lfoTone(byte side, int gainDb, int frequency, int durationMs, int lfoFrequency, int lfoDepth) {
			Report report = new Report(LFO_TONE);
			report.u8("report_id", ID_HAPTIC_LFO_TONE);
			report.u8("side", side);
			report.u8("gain_db", (byte) gainDb);
			report.u16("frequency", frequency);
			report.u16("duration_ms", durationMs);
			report.u16("lfo_freq", lfoFrequency);
			report.u8("lfo_depth", (byte) lfoDepth);
			return report.bytes();
		}

		public static byte[] logSweep(byte side, int gainDb, int durationMs, int startFrequency, int endFrequency) {
			Report report = new Report(LOG_SWEEP);
			report.u8("report_id", ID_HAPTIC_LOG_SWEEP);
			report.u8("side", side);
			report.u8("gain_db", (byte) gainDb);
			report.u16("duration_ms", durationMs);
			report.u16("start_frequency", startFrequency);
			report.u16("end_frequency", endFrequency);
			return report.bytes();
		}

		public static byte[] script(byte side, int scriptId, int gainDb) {
			Report report = new Report(SCRIPT);
			report.u8("report_id", ID_HAPTIC_SCRIPT);
			report.u8("side", side);
			report.u8("script_id", (byte) scriptId);
			report.u8("gain_db", (byte) gainDb);
			return report.bytes();
		}

		private Output() {
		}
	}

	/// Feature reports. A report is a zero report-id byte followed by a 64-byte
	/// `FeatureReportMsg`: a two-byte header, then one payload.
	public static final class Feature {
		/// Deck haptic-command side bitmasks; not valid as Triton output-report targets.
		public static final byte SIDE_LEFT = 0x01;
		public static final byte SIDE_RIGHT = 0x02;
		public static final byte SIDE_BOTH = 0x03;

		/// The size sent to the device: the report id byte plus `HID_FEATURE_REPORT_BYTES`.
		public static final int REPORT_BYTES = 65;

		public static final byte ID_TRIGGER_HAPTIC_PULSE = (byte) 0x8F;
		/// Marked "Deck only" in SDL's sources.
		public static final byte ID_TRIGGER_HAPTIC_CMD = (byte) 0xEA;
		/// Marked "Deck only" in SDL's sources.
		public static final byte ID_TRIGGER_RUMBLE_CMD = (byte) 0xEB;

		/// `FeatureReportHeader`, preceded by the report id byte.
		public static final StructLayout HEADER = MemoryLayout.structLayout(
				U8.withName("report_id"),
				U8.withName("type"),
				U8.withName("length")
		);

		/// `MsgFireHapticPulse`.
		public static final StructLayout FIRE_HAPTIC_PULSE = MemoryLayout.structLayout(
				U8.withName("which_pad"),
				U16.withName("pulse_duration"),
				U16.withName("pulse_interval"),
				U16.withName("pulse_count"),
				U16.withName("dBgain"),
				U8.withName("priority")
		);

		/// `MsgTriggerHaptic`: the general haptic command.
		public static final StructLayout TRIGGER_HAPTIC = MemoryLayout.structLayout(
				U8.withName("side"),
				U8.withName("cmd"),
				U8.withName("ui_intensity"),
				U8.withName("dBgain"),
				U16.withName("freq"),
				U16.withName("dur_ms"),
				U16.withName("noise_intensity"),
				U16.withName("lfo_freq"),
				U8.withName("lfo_depth"),
				U8.withName("rand_tone_gain"),
				U8.withName("script_id"),
				U16.withName("lss_start_freq"),
				U16.withName("lss_end_freq")
		);

		/// `MsgSimpleRumbleCmd`.
		public static final StructLayout SIMPLE_RUMBLE = MemoryLayout.structLayout(
				U8.withName("unRumbleType"),
				U16.withName("unIntensity"),
				U16.withName("unLeftMotorSpeed"),
				U16.withName("unRightMotorSpeed"),
				U8.withName("nLeftGain"),
				U8.withName("nRightGain")
		);

		/// A tone or one of the canned effects.
		///
		/// @param cmd        one of the `TYPE_` constants
		/// @param frequency  the tone in hertz, where the command has one
		/// @param durationMs how long it plays; negative for until stopped
		public static byte[] triggerHaptic(byte side, byte cmd, int gainDb, int frequency, int durationMs) {
			Report report = new Report(TRIGGER_HAPTIC, ID_TRIGGER_HAPTIC_CMD);
			report.u8("side", side);
			report.u8("cmd", cmd);
			report.u8("dBgain", (byte) gainDb);
			report.u16("freq", frequency);
			report.u16("dur_ms", durationMs);
			return report.bytes();
		}

		/// @param durationMicros how long each pulse lasts
		/// @param intervalMicros the gap between pulses
		public static byte[] fireHapticPulse(byte pad, int durationMicros, int intervalMicros, int count, int gainDb) {
			Report report = new Report(FIRE_HAPTIC_PULSE, ID_TRIGGER_HAPTIC_PULSE);
			report.u8("which_pad", pad);
			report.u16("pulse_duration", durationMicros);
			report.u16("pulse_interval", intervalMicros);
			report.u16("pulse_count", count);
			report.u16("dBgain", gainDb);
			return report.bytes();
		}

		/// @param leftSpeed  0 to 65535
		/// @param rightSpeed 0 to 65535
		public static byte[] simpleRumble(int leftSpeed, int rightSpeed, int leftGainDb, int rightGainDb) {
			Report report = new Report(SIMPLE_RUMBLE, ID_TRIGGER_RUMBLE_CMD);
			report.u16("unLeftMotorSpeed", leftSpeed);
			report.u16("unRightMotorSpeed", rightSpeed);
			report.u8("nLeftGain", (byte) leftGainDb);
			report.u8("nRightGain", (byte) rightGainDb);
			return report.bytes();
		}

		private Feature() {
		}
	}

	/// A zeroed report being filled in through its layout's field names.
	private static final class Report {
		private final byte[] bytes;
		private final MemorySegment segment;
		private final StructLayout layout;
		private final long base;

		/// An output report, which is exactly its layout.
		Report(StructLayout layout) {
			this.bytes = new byte[Math.toIntExact(layout.byteSize())];
			this.segment = MemorySegment.ofArray(this.bytes);
			this.layout = layout;
			this.base = 0;
		}

		/// A feature report: the header, then `payload`, zero-padded to the fixed size.
		Report(StructLayout payload, byte type) {
			this.bytes = new byte[Feature.REPORT_BYTES];
			this.segment = MemorySegment.ofArray(this.bytes);
			this.layout = payload;
			this.base = Feature.HEADER.byteSize();
			this.segment.set(U8, Feature.HEADER.byteOffset(groupElement("type")), type);
			this.segment.set(U8, Feature.HEADER.byteOffset(groupElement("length")), (byte) payload.byteSize());
		}

		void u8(String field, byte value) {
			this.segment.set(U8, this.base + this.layout.byteOffset(groupElement(field)), value);
		}

		void u16(String field, int value) {
			this.segment.set(U16, this.base + this.layout.byteOffset(groupElement(field)), (short) value);
		}

		byte[] bytes() {
			return this.bytes;
		}
	}

	private SteamHapticReports() {
	}
}
