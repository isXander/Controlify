package dev.isxander.controlify.controller.dualsense;

import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

public interface DS5Effect {
    void apply(DS5EffectsState state);

    record RightTriggerEffect(DS5TriggerEffect effect) implements DS5Effect {
        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_RIGHT_TRIGGER_FFB;
            state.rgucRightTriggerEffect = effect().createState();
        }
    }
    record LeftTriggerEffect(DS5TriggerEffect effect) implements DS5Effect {
        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_LEFT_TRIGGER_FFB;
            state.rgucLeftTriggerEffect = effect().createState();
        }
    }

    record HeadphoneVolume(byte volume) implements DS5Effect {
        public HeadphoneVolume {
            Validate.inclusiveBetween(0, 0x7f, volume);
        }

        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_HEADPHONE_VOLUME;
            state.ucHeadphoneVolume = volume();
        }

        public static HeadphoneVolume fromPercent(float percent) {
            return new HeadphoneVolume((byte) Mth.lerpDiscrete(percent, 0, 0x7f));
        }
    }

    record SpeakerVolume(byte volume) implements DS5Effect {
        public SpeakerVolume {
            Validate.inclusiveBetween(0x3d, 0x64, volume);
        }

        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_SPEAKER_VOLUME;
            state.ucSpeakerVolume = volume();
        }

        public static SpeakerVolume fromPercent(float percent) {
            return new SpeakerVolume((byte) Mth.lerpDiscrete(percent, 0x3d, 0x64));
        }
    }

    /**
     * Adjusts the volume of the built-in microphone
     * @param volume not linear, max 64, 0 not fully muted
     */
    record MicrophoneVolume(byte volume) implements DS5Effect {
        public MicrophoneVolume {
            Validate.inclusiveBetween(0, 64, volume);
        }

        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits1 |= DS5EffectsState.EnableBitFlags1.ALLOW_MIC_VOLUME;
            state.ucMicrophoneVolume = volume;
        }

        public static MicrophoneVolume fromPercent(float percent) {
            return new MicrophoneVolume((byte) Mth.lerpDiscrete(percent, 0, 64));
        }
    }

    record MuteLight(LightState state) implements DS5Effect {
        public MuteLight(boolean state) {
            this(state ? LightState.ON : LightState.OFF);
        }

        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits2 |= DS5EffectsState.EnableBitFlags2.ALLOW_MUTE_LIGHT;
            state.ucMicLightMode = switch (state()) {
                case OFF -> DS5EffectsState.MuteLightState.OFF;
                case ON -> DS5EffectsState.MuteLightState.ON;
                case BREATHING -> DS5EffectsState.MuteLightState.BREATHING;
            };
        }

        public enum LightState {
            OFF, ON, BREATHING
        }
    }

    record ResetLights() implements DS5Effect {
        @Override
        public void apply(DS5EffectsState state) {
            state.ucEnableBits2 |= DS5EffectsState.EnableBitFlags2.RESET_LIGHTS;
        }
    }
}
