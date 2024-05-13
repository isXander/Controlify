package dev.isxander.controlify.bindings.v2;

/**
 * An interface that is responsible for consuming a binding's raw
 * state and outputting a relevant value from that.
 */
public interface DigitalOutput {
    boolean get();
}
