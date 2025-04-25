package dev.isxander.controlify.controllermanager;

/**
 * A unique, abstract identifier for a controller that is provided and used by the underlying driver.
 * This is not used for Controlify to identify controllers, purely the native implementation during this current session.
 * The same controller may have a different {@code UniqueControllerID} each time it is connected.
 * {@link dev.isxander.controlify.controller.ControllerUID} is used to identify controllers across sessions.
 * Each implementation of {@link ControllerManager} will have a different implementation.
 */
public interface UniqueControllerID {
    @Override
    boolean equals(Object obj);

    @Override
    String toString();

    @Override
    int hashCode();
}
