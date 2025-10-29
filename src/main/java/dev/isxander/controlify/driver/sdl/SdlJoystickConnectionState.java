package dev.isxander.controlify.driver.sdl;

public enum SdlJoystickConnectionState {
    INVALID(-1),
    UNKNOWN(0),
    WIRED(1),
    WIRELESS(2);

    private final int asInt;
    SdlJoystickConnectionState(int asInt) {
        this.asInt = asInt;
    }

    public int asInt() {
        return asInt;
    }

    public static SdlJoystickConnectionState fromInt(int asInt) {
        for (SdlJoystickConnectionState state : values()) {
            if (state.asInt() == asInt) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid connection state: " + asInt);
    }
}
