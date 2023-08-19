package dev.isxander.controlify.server;

public enum ServerPolicy {
    ALLOWED,
    DISALLOWED,
    UNSET;

    public boolean isAllowed() {
        return this != DISALLOWED;
    }

    public static ServerPolicy fromBoolean(boolean value) {
        return value ? ALLOWED : DISALLOWED;
    }
}
