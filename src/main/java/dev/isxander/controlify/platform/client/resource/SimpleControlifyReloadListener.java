package dev.isxander.controlify.platform.client.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleControlifyReloadListener<T> extends ControlifyReloadListener {
    //? if >=1.21.9 {
    @Override
    default @NonNull CompletableFuture<Void> reload(
            SharedState sharedState,
            Executor loadExecutor,
            PreparationBarrier preparationBarrier,
            Executor applyExecutor
    ) {
        return reload0(preparationBarrier, sharedState.resourceManager(), loadExecutor, applyExecutor);
    }
    //?} else {
    /*@Override
    default @NonNull CompletableFuture<Void> reload(
            PreparableReloadListener.PreparationBarrier helper,
            ResourceManager manager,
            //? if <1.21.2
            /^ProfilerFiller loadProfiler, ProfilerFiller applyProfiler,^/
            Executor loadExecutor, Executor applyExecutor
    ) {
        return reload0(helper, manager, loadExecutor, applyExecutor);
    }
    *///?}

    default @NonNull CompletableFuture<Void> reload0(
            PreparationBarrier preparationBarrier,
            ResourceManager resourceManager,
            Executor loadExecutor,
            Executor applyExecutor
    ) {
        return load(resourceManager, loadExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenCompose(data -> apply(data, resourceManager, applyExecutor));
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
