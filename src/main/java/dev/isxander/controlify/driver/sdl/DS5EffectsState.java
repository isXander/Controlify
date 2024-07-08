package dev.isxander.controlify.driver.sdl;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import dev.isxander.sdl3java.jna.JnaEnum;
import org.intellij.lang.annotations.MagicConstant;

@Structure.FieldOrder({
        "ucEnableBits1", "ucEnableBits2",
        "ucRumbleRight", "ucRumbleLeft",
        "ucHeadphoneVolume", "ucSpeakerVolume", "ucMicrophoneVolume",
        "ucAudioEnableBits", "ucMicLightMode", "ucAudioMuteBits",
        "rgucRightTriggerEffect", "rgucLeftTriggerEffect",
        "unknown1",
        "ucEnableBits3",
        "unknown2",
        "ucLedAnim", "ucLedBrightness", "ucPadLights",
        "ucLedRed", "ucLedGreen", "ucLedBlue",
})
public class DS5EffectsState extends Structure {
    @MagicConstant(flagsFromClass = EnableBitFlags1.class)
    public byte ucEnableBits1;
    @MagicConstant(flagsFromClass = EnableBitFlags2.class)
    public byte ucEnableBits2;

    public byte ucRumbleRight;
    public byte ucRumbleLeft;

    public byte ucHeadphoneVolume; // max 0x7f
    public byte ucSpeakerVolume; // 0x3d-0x64 (PS range)
    public byte ucMicrophoneVolume; // not linear, max 64, 0 is not fully muted

    public byte ucAudioEnableBits;
    @MagicConstant(valuesFromClass = MagicConstant.class)
    public byte ucMicLightMode;
    public byte ucAudioMuteBits;

    public TriggerEffect rgucRightTriggerEffect;
    public TriggerEffect rgucLeftTriggerEffect;

    public byte[] unknown1 = new byte[6];

    public byte ucEnableBits3;

    public byte[] unknown2 = new byte[2];

    public byte ucLedAnim;
    public byte ucLedBrightness;
    public byte ucPadLights;
    public byte ucLedRed;
    public byte ucLedGreen;
    public byte ucLedBlue;

    public DS5EffectsState() {
        super();
    }

    public DS5EffectsState(Pointer p) {
        super(p);
    }

    public static class ByValue extends DS5EffectsState implements Structure.ByValue {
    }

    @FieldOrder({"effectType", "p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9"})
    public static class TriggerEffect extends Structure {
        public byte effectType;
        public byte p0;
        public byte p1;
        public byte p2;
        public byte p3;
        public byte p4;
        public byte p5;
        public byte p6;
        public byte p7;
        public byte p8;
        public byte p9;

        public TriggerEffect() {
            super();
        }

        public TriggerEffect(Pointer p) {
            super(p);
        }
    }

    public static final class EnableBitFlags1 {
        public static final byte
                ENABLE_RUMBLE_EMULATION = (byte) (1),
                USE_RUMBLE_NOT_HAPTICS  = (byte) (1 << 1),
                ALLOW_RIGHT_TRIGGER_FFB = (byte) (1 << 2),
                ALLOW_LEFT_TRIGGER_FFB  = (byte) (1 << 3),
                ALLOW_HEADPHONE_VOLUME  = (byte) (1 << 4),
                ALLOW_SPEAKER_VOLUME    = (byte) (1 << 5),
                ALLOW_MIC_VOLUME        = (byte) (1 << 6),
                ALLOW_AUDIO_CONTROL     = (byte) (1 << 7);

    }

    public static final class EnableBitFlags2 {
        public static final byte
                ALLOW_MUTE_LIGHT        = (byte) (1),
                ALLOW_AUDIO_MUTE        = (byte) (1 << 1),
                ALLOW_LED_COLOUR        = (byte) (1 << 2),
                RESET_LIGHTS            = (byte) (1 << 3),
                ALLOW_PLAYER_INDICATORS = (byte) (1 << 4),
                ALLOW_HAPTIC_LOW_PASS   = (byte) (1 << 5),
                ALLOW_MOTOR_POWER_LEVEL = (byte) (1 << 6),
                ALLOW_AUDIO_CONTROL_2   = (byte) (1 << 7);
    }

    public static final class MuteLightState implements JnaEnum {
        public static final byte OFF       = 0;
        public static final byte ON        = 1;
        public static final byte BREATHING = 2;
    }
}
