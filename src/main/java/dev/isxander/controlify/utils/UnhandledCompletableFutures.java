package dev.isxander.controlify.utils;

import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Returns a CompleteableFuture that does not catch Throwables, only Exceptions
 */
public final class UnhandledCompletableFutures {
    public static CompletableFuture<Void> run(Runnable runnable, Minecraft executor) {
        return supply(() -> {
            runnable.run();
            return null;
        }, executor);
    }

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier, Minecraft executor) {
        var future = new CompletableFuture<T>();
        executor.tell(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception e) { // allows Throwable to go uncaught
                future.completeExceptionally(e);

                if (e instanceof ReportedException) {
                    throw e; // rethrow a crash report
                }
            }
        });
        return future;
    }
}
