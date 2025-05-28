package dev.isxander.controlify.splitscreen.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record BundledPacketInfo(boolean includeController, Collection<Integer> pawnIndexes) {

    public static final StreamCodec<FriendlyByteBuf, BundledPacketInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            BundledPacketInfo::includeController,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT),
            BundledPacketInfo::pawnIndexes,
            BundledPacketInfo::new
    );

    public static BundledPacketInfo create(SplitscreenPlayerInfo.Controller controller, List<ServerPlayer> players) {
        boolean includeController = players.contains(controller.player());
        List<Integer> pawnIndexes = controller.subPlayerInfos().stream()
                .map(SplitscreenPlayerInfo.SubPlayer::pawnIndex)
                .toList();

        return new BundledPacketInfo(includeController, pawnIndexes);
    }
}
