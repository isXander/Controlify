package dev.isxander.controlify.controller.dualsense;

public final class DS5TriggerEffectTypes {

    private DS5TriggerEffectTypes() {
    }

    public static final byte
            // Officially recognised modes
            // These are 100% safe and are the only effects that modify the trigger status nibble
            OFF = 0x05,       // 00 00 0 101
            FEEDBACK = 0x21,  // 00 10 0 001
            WEAPON = 0x25,    // 00 10 0 101
            VIBRATION = 0x26, // 00 10 0 110

            // Unofficial but unique effects left in the firmware
            // These might be removed in the future
            BOW = 0x22,       // 00 10 0 010
            GALLOPING = 0x23, // 00 10 0 011
            MACHINE = 0x27,   // 00 10 0 111

            // Leftover versions of official modes with simpler logic and no parameter protections
            // These should not be used
            SIMPLE_FEEDBACK = 0x01,  // 00 00 0 001
            SIMPLE_WEAPON = 0x02,    // 00 00 0 010
            SIMPLE_VIBRATION = 0x03, // 00 00 0 011

            // Leftover versions of official modes with limited parameter ranges
            // These should not be used
            LIMITED_FEEDBACK = 0x11,  // 00 01 0 001
            LIMITED_WEAPON = 0x12,    // 00 01 0 010

            // Debug or calibration functions
            // Don't use these as they will corrupt the trigger state until the reset button is pressed
            DEBUG_FC = (byte) 0xFC, // 11 11 1 100
            DEBUG_FD = (byte) 0xFD, // 11 11 1 101
            DEBUG_FE = (byte) 0xFE; // 11 11 1 110
}
