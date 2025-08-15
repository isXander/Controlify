package dev.isxander.controlify.screenop.keyboard;

/**
 * Represents the behaviour of a GUI component with respect to keyboard input.
 * This is used by {@link dev.isxander.controlify.screenop.ScreenProcessor} to
 * determine how/if to open a keyboard for this component when it is pressed.
 */
public sealed interface ComponentKeyboardBehaviour {

    Undefined UNDEFINED = new Undefined();

    /**
     * No behaviour has been defined for this component.
     * This is the default behaviour and indicates that the component does not support
     * keyboard input or that the behaviour is not yet defined.
     */
    record Undefined() implements ComponentKeyboardBehaviour {}

    /**
     * Indicates that some other support has been provided, for example by the screen itself
     * and a separate keyboard widget that supports it.
     */
    record DoNothing() implements ComponentKeyboardBehaviour {}

    /**
     * Custom support for this gui component for a popup keyboard.
     * @param layout the keyboard layout to use
     * @param inputTarget the input target for the keyboard
     * @param positioner the positioner for the overlaid keyboard
     */
    record Handled(
            KeyboardLayoutWithId layout,
            InputTarget inputTarget,
            KeyboardOverlayScreen.KeyboardPositioner positioner
    ) implements ComponentKeyboardBehaviour {}
}
