package dev.isxander.controlify.bindings.v2;

public interface StateAccess {
    float analogue(int history);

    boolean digital(int history);

    boolean isSuppressed();

    boolean isValid();

    int maxHistory();
}
