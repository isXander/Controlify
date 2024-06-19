package dev.isxander.controlify.platform.client.resource;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface SimpleControlifyReloadListener<T> extends ControlifyReloadListener {
    @Override
    default @NotNull CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier helper, ResourceManager manager, ProfilerFiller loadProfiler, ProfilerFiller applyProfiler, Executor loadExecutor, Executor applyExecutor) {
        return load(manager, loadProfiler, loadExecutor).thenCompose(helper::wait).thenCompose(
                (o) -> apply(o, manager, applyProfiler, applyExecutor)
        );
    }

    /**
     * Asynchronously process and load resource-based data. The code
     * must be thread-safe and not modify game state!
     *
     * @param manager  The resource manager used during reloading.
     * @param profiler The profiler which may be used for this stage.
     * @param executor The executor which should be used for this stage.
     * @return A CompletableFuture representing the "data loading" stage.
     */
    CompletableFuture<T> load(ResourceManager manager, ProfilerFiller profiler, Executor executor);

    /**
     * Synchronously apply loaded data to the game state.
     *
     * @param manager  The resource manager used during reloading.
     * @param profiler The profiler which may be used for this stage.
     * @param executor The executor which should be used for this stage.
     * @return A CompletableFuture representing the "data applying" stage.
     */
    CompletableFuture<Void> apply(T data, ResourceManager manager, ProfilerFiller profiler, Executor executor);
}
