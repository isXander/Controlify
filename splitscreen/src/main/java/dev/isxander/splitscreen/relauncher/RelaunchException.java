package dev.isxander.splitscreen.relauncher;

public class RelaunchException extends RuntimeException {
    public RelaunchException(String message) {
        super(message);
    }

    public RelaunchException(String message, Throwable cause) {
        super(message, cause);
    }
}
