package dev.isxander.splitscreen.client.host.features.relaunch;

/**
 * Represents the loading status of a relaunched client
 */
public sealed interface PendingRelaunchClientStatus {
    record WaitingForConnection() implements PendingRelaunchClientStatus {}

    record WaitingForReadySignal(float progress) implements PendingRelaunchClientStatus {}
}
