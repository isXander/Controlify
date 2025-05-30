package dev.isxander.splitscreen.client.host.gui;

import dev.isxander.splitscreen.client.host.features.relaunch.PendingRelaunchClientStatus;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A reload instance to pass to {@link net.minecraft.client.gui.screens.LoadingOverlay} which just
 * reads the progress and nothing else.
 */
public class SplitscreenFakeReloadInstance implements ReloadInstance {
    private final CompletableFuture<?> future;
    private final Collection<PendingRelaunchClientStatus> waitingOn;
    private int maxSeen = 0;

    public SplitscreenFakeReloadInstance(Collection<PendingRelaunchClientStatus> waitingOn) {
        this.future = new CompletableFuture<>();
        this.waitingOn = waitingOn;
    }

    @Override
    public CompletableFuture<?> done() {
        return this.future;
    }

    @Override
    public float getActualProgress() {
        // keep track of the max in the waitingOn list, so the progress bar doesn't jump back
        this.maxSeen = Math.max(this.maxSeen, this.waitingOn.size());

        // amount of clients that have been removed from the waitingOn list, (i.e. finished)
        int finishedClients = this.maxSeen - this.waitingOn.size();

        // gets a sum of the progresses of all the clients we are waiting for
        float workingSumProgress = (float) this.waitingOn.stream()
                .mapToDouble(status -> switch (status) {
                    case PendingRelaunchClientStatus.WaitingForConnection ignored -> 0.05;
                    // 0.25 is the jump that is made when the client goes from waiting for connection -> waiting for ready
                    case PendingRelaunchClientStatus.WaitingForReadySignal(float progress) -> 0.25 + (Mth.clamp(progress, 0, 1) * (1 - 0.25 - 0.05));
                })
                .sum();
        // finishedClients represent a progress of 1.0
        // no single waitingOn progress can go beyond 0.95
        float sumProgress = workingSumProgress + finishedClients;

        float progress = sumProgress / this.maxSeen;

        // progress can only ever be 1 if waitingOn becomes empty
        if (progress >= 1.0) {
            this.future.complete(null);
        }

        return progress;
    }
}
