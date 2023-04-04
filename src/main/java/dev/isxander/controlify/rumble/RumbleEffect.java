package dev.isxander.controlify.rumble;

public interface RumbleEffect  {
    RumbleState nextState();

    boolean isFinished();

    int priority();
}
