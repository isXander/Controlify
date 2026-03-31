package dev.isxander.controlify.platform.client.resource;

import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleControlifyReloadListener<T> extends ControlifyReloadListener {
    @Override
    default @NotNull CompletableFuture<Void> reload(
            SharedState sharedState,
            @NonNull Executor loadExecutor,
            @NonNull PreparationBarrier preparationBarrier,
            @NonNull Executor applyExecutor
    ) {
        return reload0(preparationBarrier, sharedState.resourceManager(), loadExecutor, applyExecutor);
    }

    default @NotNull CompletableFuture<Void> reload0(
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
