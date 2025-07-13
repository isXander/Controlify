package dev.isxander.splitscreen.client.host;

import dev.isxander.splitscreen.client.InputMethod;
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

    private final int index;
    private final InputMethod associatedInputMethod;

    /**
     * Creates a new handle to a remote splitscreen pawn.
     * @param connection connection to a remote client
     * @param associatedInputMethod the controller uid associated with this pawn
     */
    public RemoteSplitscreenPawn(Connection connection, int index, InputMethod associatedInputMethod) {
        this.connection = connection;
        this.index = index;
        this.associatedInputMethod = associatedInputMethod;
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
    public void useInputMethod(InputMethod inputMethod) {
        this.connection.send(new PawnboundUseInputMethodPacket(inputMethod));
    }

    @Override
    public void onConfigSave(ResourceLocation config) {
        this.connection.send(new PawnboundLoadConfigPacket(config));
    }

    @Override
    public InputMethod getAssociatedInputMethod() {
        return this.associatedInputMethod;
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
