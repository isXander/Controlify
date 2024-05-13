package dev.isxander.controlify.bindings.v2;

public interface StateAccess {
    float analogue(int history);

    boolean digital(int history);

    boolean isValid();

    int maxHistory();
}
