package dev.isxander.controlify.controller.dualsense;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface DS5TriggerEffect {
    DS5EffectsState.TriggerEffect createState();

    /**
     * Turn the trigger effect off and return the trigger stop to the neutral position.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     */
    record Off() implements DS5TriggerEffect {
        @Override
        public DS5EffectsState.TriggerEffect createState() {
            return DS5EffectsState.TriggerEffect.OFF;
        }
    }

    /**
     * Trigger will resist movement beyond the start position.
     * The trigger status nybble will report 0 before the effect and 1 when in the effect.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @param position The starting zone of the trigger effect. Must be between 0 and 9 inclusive.
     * @param strength The force of the resistance. Must be between 0 and 8 inclusive.
     */
    record Feedback(
            @Range(from = 0, to = 9) byte position,
            @Range(from = 0, to = 8) byte strength
    ) implements DS5TriggerEffect {
        public Feedback {
            Validate.inclusiveBetween(0, 9, position, "Position must be between 0 and 9 inclusive");
            Validate.inclusiveBetween(0, 8, strength, "Strength must be between 0 and 8 inclusive");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            if (strength > 0) {
                byte forceValue = (byte) ((strength - 1) & 0x07);
                int forceZones = 0;
                char activeZones = 0;
                for (int i = position; i < 10; i++) {
                    forceZones |= (forceValue << (3 * i));
                    activeZones |= (char) (1 << i);
                }

                return new DS5EffectsState.TriggerEffect(DS5TriggerEffectTypes.FEEDBACK, new byte[]{
                        (byte) (activeZones & 0xff),
                        (byte) ((activeZones >> 8) & 0xff),
                        (byte) (forceZones & 0xff),
                        (byte) ((forceZones >> 8) & 0xff),
                        (byte) ((forceZones >> 16) & 0xff),
                        (byte) ((forceZones >> 24) & 0xff),
                });
            } else {
                return DS5EffectsState.TriggerEffect.OFF;
            }
        }
    }

    /**
     * Trigger will resist movement beyond the start position until the end position.
     * The trigger status nybble will report 0 before the effect and 1 when in the effect,
     * and 2 after until again before the start position.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @param startPosition The starting zone of the trigger effect. Must be between 2 and 7 inclusive.
     * @param endPosition The ending zone of the trigger effect. Must be between start+1 and 8 inclusive.
     * @param strength The force of the resistance. Must be between 0 and 8 inclusive.
     */
    record Weapon(
            @Range(from = 2, to = 7) byte startPosition,
            @Range(from = 2+1, to = 8) byte endPosition,
            @Range(from = 0, to = 8) byte strength
    ) implements DS5TriggerEffect {
        public Weapon {
            Validate.inclusiveBetween(2, 7, startPosition, "Start position must be between 2 and 7 inclusive");
            Validate.inclusiveBetween(startPosition+1, 8, endPosition, "End position must be between start+1 and 8 inclusive");
            Validate.inclusiveBetween(0, 8, strength, "Strength must be between 0 and 8 inclusive");
            Validate.isTrue(startPosition < endPosition, "Start position must be less than end position");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            if (strength > 0) {
                char startAndStopZones = (char) ((1 << startPosition) | (1 << endPosition));

                return new DS5EffectsState.TriggerEffect(DS5TriggerEffectTypes.WEAPON, new byte[]{
                        (byte) (startAndStopZones & 0xff),
                        (byte) ((startAndStopZones >> 8) & 0xff),
                        (byte) (strength - 1), // this is actually packed into 3 bits, but since it's only one why bother with the fancy code?
                });
            } else {
                return DS5EffectsState.TriggerEffect.OFF;
            }
        }
    }

    /**
     * Trigger will vibrate with the input amplitude and frequency beyond the start position.
     * The trigger status nybble will report 0 before the effect and 1 when in the effect.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @see VibrationMultiplePosition
     *
     * @param position The starting zone of the trigger effect. Must be between 0 and 9 inclusive.
     * @param amplitude Strength of the automatic cycling action. Must be between 0 and 8 inclusive.
     * @param frequency Frequency of the automatic cycling action in hertz.
     */
    record Vibration(
            @Range(from = 0, to = 9) byte position,
            @Range(from = 0, to = 8) byte amplitude,
            byte frequency
    ) implements DS5TriggerEffect {
        public Vibration {
            Validate.inclusiveBetween(0, 9, position, "Position must be between 0 and 9 inclusive");
            Validate.inclusiveBetween(0, 8, amplitude, "Amplitude must be between 0 and 8 inclusive");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            if (amplitude > 0 && frequency > 0) {
                byte strengthValue = (byte) ((amplitude - 1) & 0x07);
                int amplitudeZones = 0;
                char activeZones = 0;

                for (int i = position; i < 10; i++) {
                    amplitudeZones |= (strengthValue << (3 * i));
                    activeZones |= (char) (1 << i);
                }

                return new DS5EffectsState.TriggerEffect(DS5TriggerEffectTypes.VIBRATION, new byte[]{
                        (byte) (activeZones & 0xff),
                        (byte) ((activeZones >> 8) & 0xff),
                        (byte) (amplitudeZones & 0xff),
                        (byte) ((amplitudeZones >> 8) & 0xff),
                        (byte) ((amplitudeZones >> 16) & 0xff),
                        (byte) ((amplitudeZones >> 24) & 0xff),
                        0, 0,
                        frequency,
                });
            } else {
                return DS5EffectsState.TriggerEffect.OFF;
            }
        }
    }

    /**
     * Trigger will resist movement at varying strengths in 10 regions.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @see Feedback
     * @see FeedbackSlope
     *
     * @param strength Array of 10 resistance values for zones 0 through 9. Must be between 0 and 8 inclusive.
     */
    record FeedbackMultiplePosition(
            @Range(from = 0, to = 9) byte @NotNull [] strength
    ) implements DS5TriggerEffect {
        public FeedbackMultiplePosition {
            Validate.notNull(strength, "Strength array must not be null");
            Validate.isTrue(strength.length == 10, "Strength array must have 10 elements");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            boolean allZero = true;
            for (int i = 0; i < 10; i++) {
                allZero &= strength[i] == 0;
                Validate.inclusiveBetween(0, 8, strength[i], "Strength i=%s must be between 0 and 8 inclusive".formatted(i));
            }

            if (!allZero) {
                int forceZones = 0;
                char activeZones = 0;

                for (int i = 0; i < 10; i++) {
                    byte strengthValue = strength[i];
                    if (strengthValue > 0) {
                        byte forceValue = (byte) ((strengthValue - 1) & 0x07);
                        forceZones |= (forceValue << (3 * i));
                        activeZones |= (char) (1 << i);
                    }
                }

                return new DS5EffectsState.TriggerEffect(DS5TriggerEffectTypes.FEEDBACK, new byte[]{
                        (byte) (activeZones & 0xff),
                        (byte) ((activeZones >> 8) & 0xff),
                        (byte) (forceZones & 0xff),
                        (byte) ((forceZones >> 8) & 0xff),
                        (byte) ((forceZones >> 16) & 0xff),
                        (byte) ((forceZones >> 24) & 0xff),
                });
            } else {
                return DS5EffectsState.TriggerEffect.OFF;
            }
        }
    }

    /**
     * Trigger will resist movement at a linear range of strengths.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @see Feedback
     * @see FeedbackMultiplePosition
     *
     * @param startPosition The starting zone of the trigger effect. Must be between 0 and 8 inclusive.
     * @param endPosition The ending zone of the trigger effect. Must be between start+1 and 9 inclusive.
     * @param startStrength The force of the resistance at the start position. Must be between 1 and 8 inclusive.
     * @param endStrength The force of the resistance at the end. Must be between 1 and 8 inclusive.
     */
    record FeedbackSlope(
            @Range(from = 0, to = 8) byte startPosition,
            @Range(from = 1, to = 9) byte endPosition,
            @Range(from = 1, to = 8) byte startStrength,
            @Range(from = 1, to = 8) byte endStrength
    ) implements DS5TriggerEffect {
        public FeedbackSlope {
            Validate.inclusiveBetween(0, 8, startPosition, "Start position must be between 0 and 8 inclusive");
            Validate.inclusiveBetween(startPosition+1, 9, endPosition, "End position must be between start+1 and 9 inclusive");
            Validate.inclusiveBetween(1, 8, startStrength, "Start strength must be between 1 and 8 inclusive");
            Validate.inclusiveBetween(1, 8, endStrength, "End strength must be between 1 and 8 inclusive");
            Validate.isTrue(startPosition < endPosition, "Start strength must be less than end position");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            byte[] strength = new byte[10];
            float gradient = (endStrength - startStrength) / (float) (endPosition - startPosition);
            for (int i = startPosition; i < 10; i++) {
                strength[i] = i <= endPosition
                        ? (byte) Math.round(startStrength + gradient * (i - startPosition))
                        : endStrength;
            }

            return new FeedbackMultiplePosition(strength).createState();
        }
    }

    /**
     * Trigger will vibrate movement at varying amplitudes and one frequency in 10 regions.
     * <p>
     * This is an official effect and is expected to be present in future DualSense firmware versions.
     *
     * @see Vibration
     *
     * @param frequency Frequency of the automatic cycling action in hertz.
     * @param amplitude Array of 10 strength values for zones 0 through 9. Must between 0 and 8 inclusive.
     */
    record VibrationMultiplePosition(
            byte frequency,
            @Range(from = 0, to = 8) byte @NotNull [] amplitude
    ) implements DS5TriggerEffect {
        public VibrationMultiplePosition {
            Validate.notNull(amplitude, "Amplitude array must not be null");
            Validate.isTrue(amplitude.length == 10, "Amplitude array must have 10 elements");
        }

        @Override
        public DS5EffectsState.TriggerEffect createState() {
            if (frequency > 0) {
                boolean allZero = true;
                for (int i = 0; i < 10; i++) {
                    allZero &= amplitude[i] == 0;
                    Validate.inclusiveBetween(0, 8, amplitude[i], "Amplitude i=%s must be between 0 and 8 inclusive".formatted(i));
                }

                if (!allZero) {
                    int strengthZones = 0;
                    char activeZones = 0;

                    for (int i = 0; i < 10; i++) {
                        byte amplitudeValue = amplitude[i];

                        if (amplitudeValue > 0) {
                            byte strengthValue = (byte) ((amplitudeValue - 1) & 0x07);
                            strengthZones |= (strengthValue << (3 * i));
                            activeZones |= (char) (1 << i);
                        }
                    }

                    return new DS5EffectsState.TriggerEffect(DS5TriggerEffectTypes.VIBRATION, new byte[]{
                            (byte) (activeZones & 0xff),
                            (byte) ((activeZones >> 8) & 0xff),
                            (byte) (strengthZones & 0xff),
                            (byte) ((strengthZones >> 8) & 0xff),
                            (byte) ((strengthZones >> 16) & 0xff),
                            (byte) ((strengthZones >> 24) & 0xff),
                            0, 0,
                            frequency,
                    });
                }
            }

            return DS5EffectsState.TriggerEffect.OFF;
        }
    }
}
