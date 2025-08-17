package dev.isxander.controlify.input.action;

public enum ConsumePolicy {
    /** Will not consume the signal, allowing other gestures to also process it. */
    NON_CONSUMING,
    /** Will consume the signal and prevent other bindings from using it */
    CONSUME,
}
