package dev.isxander.splitscreen.client.host;

import dev.isxander.controlify.controller.ControllerUID;
import dev.isxander.splitscreen.client.SplitscreenPawn;
import dev.isxander.splitscreen.client.SplitscreenPosition;
import dev.isxander.splitscreen.client.ipc.packets.pawnbound.play.*;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A splitscreen pawn object that remotely controls another client using packets.
 */
public class RemoteSplitscreenPawn implements SplitscreenPawn {
    private final Connection connection;

    private SplitscreenPosition position = null;
    private final int index;
    private final @Nullable ControllerUID associatedController;

    /**
     * Creates a new handle to a remote splitscreen pawn.
     * @param connection connection to a remote client
     * @param associatedController the controller uid associated with this pawn
     */
    public RemoteSplitscreenPawn(Connection connection, int index, @Nullable ControllerUID associatedController) {
        this.connection = connection;
        this.index = index;
        this.associatedController = associatedController;
    }

    @Override
    public int pawnIndex() {
        return this.index;
    }

    @Override
    public void joinServer(String serverAddress, int serverPort, byte @Nullable [] nonce) {
        this.connection.send(new PawnboundJoinServerPacket(serverAddress, serverPort, Optional.ofNullable(nonce)));
    }

    @Override
    public void closeGame() {
        this.connection.send(new PawnboundCloseGamePacket());
    }

    @Override
    public void disconnectFromServer() {
        this.connection.send(PawnboundServerDisconnectPacket.UNIT);
    }

    @Override
    public void useController(ControllerUID controllerUid) {
        this.connection.send(new PawnboundUseControllerPacket(controllerUid));
    }

    @Override
    public void onConfigSave(ResourceLocation config) {
        this.connection.send(new PawnboundLoadConfigPacket(config));
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
