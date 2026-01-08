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
     * time to register events in preparation for impl discovery.
     */
    void onControlifyInit(InitContext context);


    /**
     * Called at the end of Controlify's client-side entrypoint.
     * You can register guides here.
     */
    void onControlifyPreInit(PreInitContext context);

}
