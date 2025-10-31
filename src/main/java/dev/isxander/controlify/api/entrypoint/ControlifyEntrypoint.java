package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.ControlifyApi;

public interface ControlifyEntrypoint {
    /**
     * Called once Controlify has been fully initialised. And all controllers have
     * been discovered and loaded.
     * Due to the nature of the resource-pack system, this is called
     * very late in the game's lifecycle (once the resources have been reloaded).
     */
    void onControllersDiscovered(ControlifyApi controlify);

    /**
     * Called once Controlify has initialised some systems but controllers
     * have not yet been discovered and constructed. This is the ideal
     * time to register events in preparation for controller discovery.
     * Input bindings cannot be registered here; use {@link #onControlifyPreInit} instead.
     */
    void onControlifyInit(InitContext context);


    /**
     * Called at the end of Controlify's client-side entrypoint.
     * Use this to register guides, input bindings, radial icons, or screen processors,
     * and to subscribe to events from {@link dev.isxander.controlify.api.event.ControlifyEvents}.
     * Avoid referencing objects that are registered later (e.g. custom mod items),
     * as they may not be initialized yet and could cause errors due to deferred registration.
     */
    void onControlifyPreInit(PreInitContext context);

}
