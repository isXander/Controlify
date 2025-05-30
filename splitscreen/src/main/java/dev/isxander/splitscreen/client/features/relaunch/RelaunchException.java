package dev.isxander.splitscreen.client.features.relaunch;

public class RelaunchException extends RuntimeException {
    public RelaunchException(String message) {
        super(message);
    }

    public RelaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}
