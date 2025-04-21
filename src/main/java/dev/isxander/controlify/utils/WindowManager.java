package dev.isxander.controlify.utils;

public enum WindowManager {
    UNKNOWN,
    X11,
    WAYLAND,
    WIN32,
    COCOA; // macOS

    public static final WindowManager INSTANCE;

    static {
        Platform platform = Platform.current();
        if (platform == Platform.MAC) {
            INSTANCE = COCOA;
        } else if (platform == Platform.WINDOWS) {
            INSTANCE = WIN32;
        } else if (platform == Platform.LINUX) {
            String session = System.getenv("XDG_SESSION_TYPE");
            if ("wayland".equalsIgnoreCase(session)) {
                INSTANCE = WAYLAND;
            } else {
                INSTANCE = X11;
            }
        } else {
            INSTANCE = UNKNOWN;
        }
    }
}
