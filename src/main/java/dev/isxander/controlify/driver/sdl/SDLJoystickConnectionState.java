package dev.isxander.controlify.driver.sdl;

public enum SDLJoystickConnectionState {
    INVALID(-1),
    UNKNOWN(0),
    WIRED(1),
    WIRELESS(2);

    private final int asInt;
    SDLJoystickConnectionState(int asInt) {
        this.asInt = asInt;
    }

    public int asInt() {
        return asInt;
    }

    public static SDLJoystickConnectionState fromInt(int asInt) {
        for (SDLJoystickConnectionState state : values()) {
            if (state.asInt() == asInt) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid connection state: " + asInt);
    }
}
