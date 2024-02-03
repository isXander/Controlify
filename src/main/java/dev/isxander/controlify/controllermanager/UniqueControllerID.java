package dev.isxander.controlify.controllermanager;

/**
 * A unique, abstract identifier for a controller.
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
