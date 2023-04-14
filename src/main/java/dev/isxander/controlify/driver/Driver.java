package dev.isxander.controlify.driver;

public interface Driver {
    void update();

    default void close() {
    }
}
