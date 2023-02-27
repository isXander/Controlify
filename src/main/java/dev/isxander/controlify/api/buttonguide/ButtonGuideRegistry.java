package dev.isxander.controlify.api.buttonguide;

import dev.isxander.controlify.bindings.ControllerBinding;

/**
 * Allows you to register your own actions to the button guide.
 * This should be called through {@link dev.isxander.controlify.api.event.ControlifyEvents#BUTTON_GUIDE_REGISTRY} as
 * these should be called every time the guide is initialised.
 */
public interface ButtonGuideRegistry {
    /**
     * Registers a new action to the button guide.
     *
     * @param binding the binding for the action, if unbound, the action is hidden.
     * @param location the location of the action, left or right.
     * @param priority the priority of the action, used to sort the list.
     * @param supplier the supplier for the name of the action. can be empty to hide the action.
     */
    void registerGuideAction(ControllerBinding<?> binding, ActionLocation location, ActionPriority priority, GuideActionNameSupplier supplier);

    /**
     * Registers a new action to the button guide.
     *
     * @param binding the binding for the action, if unbound, the action is hidden.
     * @param location the location of the action, left or right.
     * @param supplier the supplier for the name of the action. can be empty to hide the action.
     */
    void registerGuideAction(ControllerBinding<?> binding, ActionLocation location, GuideActionNameSupplier supplier);
}
