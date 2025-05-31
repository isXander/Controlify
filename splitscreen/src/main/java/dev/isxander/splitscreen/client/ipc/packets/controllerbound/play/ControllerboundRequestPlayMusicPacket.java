package dev.isxander.splitscreen.client.ipc.packets.controllerbound.play;

import dev.isxander.splitscreen.client.host.ipc.ControllerPlayPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.sounds.Music;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ControllerboundRequestPlayMusicPacket(Optional<Music> music, float volume) implements ControllerboundPlayPacket {

    public ControllerboundRequestPlayMusicPacket(@Nullable Music music, float volume) {
        this(Optional.ofNullable(music), volume);
    }

    public static final StreamCodec<FriendlyByteBuf, ControllerboundRequestPlayMusicPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.fromCodec(Music.CODEC)),
            ControllerboundRequestPlayMusicPacket::music,
            ByteBufCodecs.FLOAT,
            ControllerboundRequestPlayMusicPacket::volume,
            ControllerboundRequestPlayMusicPacket::new
    );
    public static final PacketType<ControllerboundRequestPlayMusicPacket> TYPE = ControllerboundPlayPacket.createType("request_play_music");

    @Override
    public void handle(ControllerPlayPacketListener handler) {

    }

    @Override
    public PacketType<ControllerboundRequestPlayMusicPacket> type() {
        return TYPE;
    }
}
