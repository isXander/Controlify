package dev.isxander.controlify.bindings.output;

/**
 * An interface that is responsible for consuming a binding's raw
 * state and outputting a relevant value from that.
 */
public interface DigitalOutput extends BindingOutput {
    boolean get();
}
