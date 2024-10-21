package dev.isxander.controlify.platform.client.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleControlifyReloadListener<T> extends ControlifyReloadListener {
    @Override
    default @NotNull CompletableFuture<Void> reload(
            PreparableReloadListener.PreparationBarrier helper,
            ResourceManager manager,
            //? if <1.21.2
            /*ProfilerFiller loadProfiler, ProfilerFiller applyProfiler,*/
            Executor loadExecutor, Executor applyExecutor
    ) {
        return load(manager, loadExecutor).thenCompose(helper::wait).thenCompose(
                (o) -> apply(o, manager, applyExecutor)
        );
    }

    /**
     * Asynchronously process and load resource-based data. The code
     * must be thread-safe and not modify game state!
     *
     * @param manager  The resource manager used during reloading.
     * @param executor The executor which should be used for this stage.
     * @return A CompletableFuture representing the "data loading" stage.
     */
    CompletableFuture<T> load(ResourceManager manager, Executor executor);

    /**
     * Synchronously apply loaded data to the game state.
     *
     * @param manager  The resource manager used during reloading.
     * @param executor The executor which should be used for this stage.
     * @return A CompletableFuture representing the "data applying" stage.
     */
    CompletableFuture<Void> apply(T data, ResourceManager manager, Executor executor);
}
