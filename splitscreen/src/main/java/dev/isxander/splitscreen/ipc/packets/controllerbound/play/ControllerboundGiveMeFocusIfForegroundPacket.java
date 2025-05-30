package dev.isxander.splitscreen.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.host.ipc.ControllerPlayPacketListener;
import dev.isxander.splitscreen.engine.impl.reparenting.manager.NativeWindowHandle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;

public record ControllerboundGiveMeFocusIfForegroundPacket(NativeWindowHandle childWindow) implements ControllerboundPlayPacket {
    public static final StreamCodec<FriendlyByteBuf, ControllerboundGiveMeFocusIfForegroundPacket> CODEC =
            NativeWindowHandle.STREAM_CODEC.map(ControllerboundGiveMeFocusIfForegroundPacket::new, ControllerboundGiveMeFocusIfForegroundPacket::childWindow);
    public static final PacketType<ControllerboundGiveMeFocusIfForegroundPacket> TYPE =
            ControllerboundPlayPacket.createType("give_me_focus_if_foreground");

    @Override
    public void handle(ControllerPlayPacketListener handler) {
        handler.handleGiveChildFocusIfForeground(this);
    }

    @Override
    public PacketType<ControllerboundGiveMeFocusIfForegroundPacket> type() {
        return TYPE;
    }
}
