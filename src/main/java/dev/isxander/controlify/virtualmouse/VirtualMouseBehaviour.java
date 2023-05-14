package dev.isxander.controlify.virtualmouse;

public enum VirtualMouseBehaviour {
    DEFAULT,
    ENABLED,
    DISABLED,
    CURSOR_ONLY;

    public boolean hasCursor() {
        return this != DISABLED;
    }
}
