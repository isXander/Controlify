package dev.isxander.splitscreen.client.host;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.ControllerBridge;
import dev.isxander.splitscreen.client.SplitscreenPawn;
import dev.isxander.splitscreen.client.host.gui.SplitscreenDisconnectedScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ControllerBridge} for when this client is the controller.
 * A thin bridge that directly executes code.
 */
public class LocalControllerBridge implements ControllerBridge {

    private final Minecraft minecraft;
    private final SplitscreenController controller;

    public LocalControllerBridge(Minecraft minecraft, SplitscreenController controller) {
        this.minecraft = minecraft;
        this.controller = controller;
    }

    @Override
    public void giveFocusToMeIfForeground() {
//        this.giveFocusToChildIfForeground(
//                WindowManager.get().getNativeWindowHandle(
//                        minecraft.getWindow().getWindow()
//                ),
//                controller.getLocalPawn()
//        );
    }

    @Override
    public void signalImReady(boolean finished, float progress) {
        // no-op
        // The controller is always ready, so we don't need to signal anything.
    }

    @Override
    public void serverDisconnected(Component reason) {
        this.serverDisconnectedRemote(reason, null);
    }

    public void serverDisconnectedRemote(Component reason, RemoteSplitscreenPawn causePawn) {
        this.controller.forEachPawn(pawn -> {
            pawn.disconnectFromServer();
            if (pawn instanceof HostLocalSplitscreenPawn) {
                Component title = causePawn != null
                        ? Component.translatable("controlify.splitscreen.disconnect.remote_pawn", causePawn.pawnIndex())
                        : Component.translatable("controlify.splitscreen.disconnect.host_pawn");
                this.minecraft.setScreen(new SplitscreenDisconnectedScreen(this.minecraft.screen, title, reason));
            }
        });
    }

    public void signalRemoteClientReady(boolean finished, float progress, RemoteSplitscreenPawn pawn, @Nullable ControllerUID associatedController) {
        this.controller.onPawnReadySignal(finished, progress, pawn, associatedController);
    }

    @Override
    public void requestPlayMusic(@Nullable Music music, float volume) {
        this.requestPlayMusicRemote(music, volume, this.controller.getLocalPawn());
    }

    public void requestPlayMusicRemote(@Nullable Music music, float volume, SplitscreenPawn pawn) {
        this.controller.getPawnMusicManager().onRequest(music, volume, pawn);
    }

    @Override
    public boolean isRemote() {
        return false;
    }
}
