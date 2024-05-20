package dev.isxander.controlify.bindings;

public interface StateAccess {
    float analogue(int history);

    boolean digital(int history);

    boolean isSuppressed();

    boolean isValid();

    int maxHistory();
}
