package dev.isxander.controlify.splitscreen.host;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.ipc.packets.pawnbound.play.*;
import dev.isxander.controlify.splitscreen.SplitscreenPosition;
import dev.isxander.controlify.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

/**
 * A splitscreen pawn object that remotely controls another client using packets.
 */
public class RemoteSplitscreenPawn implements SplitscreenPawn {
    private final Connection connection;

    private SplitscreenPosition position = null;
    private final @Nullable ControllerUID associatedController;

    /**
     * Creates a new handle to a remote splitscreen pawn.
     * @param connection connection to a remote client
     * @param associatedController the controller uid associated with this pawn
     */
    public RemoteSplitscreenPawn(Connection connection, @Nullable ControllerUID associatedController) {
        this.connection = connection;
        this.associatedController = associatedController;
    }

    @Override
    public void joinServer(String serverAddress, int serverPort) {
        this.connection.send(new PawnboundJoinServerPacket(serverAddress, serverPort));
    }

    @Override
    public void closeGame() {
        this.connection.send(new PawnboundCloseGamePacket());
    }

    @Override
    public void useController(ControllerUID controllerUid) {
        this.connection.send(new PawnboundUseControllerPacket(controllerUid));
    }

    @Override
    public SplitscreenPosition getWindowSplitscreenMode() {
        return this.position;
    }

    @Override
    public @Nullable ControllerUID getAssociatedController() {
        return this.associatedController;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
