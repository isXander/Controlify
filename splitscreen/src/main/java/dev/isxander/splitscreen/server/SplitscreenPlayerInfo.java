package dev.isxander.splitscreen.server;

import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.config.SplitscreenServerSharedConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Data attached to {@link ServerPlayer} objects, it's a server-side way to describe
 * a specific player within a splitscreen system.
 */
public sealed interface SplitscreenPlayerInfo {
    static Optional<SplitscreenPlayerInfo> get(ServerPlayer player) {
        return Optional.ofNullable(((SplitscreenPlayerInfoHolder) player).splitscreen$getPlayerInfo());
    }
    static Optional<Controller> getController(ServerPlayer player) {
        return get(player)
                .filter(SplitscreenPlayerInfo::isController)
                .map(info -> (Controller) info);
    }
    static Optional<SubPlayer> getSubPlayer(ServerPlayer player) {
        return get(player)
                .filter(SplitscreenPlayerInfo::isSubPlayer)
                .map(info -> (SubPlayer) info);
    }

    Controller controller();

    SplitscreenServerSharedConfig sharedConfig();

    boolean isController();
    boolean isSubPlayer();

    ServerPlayer player();

    record Controller(GameProfile[] subPlayerProfiles, SplitscreenServerSharedConfig sharedConfig, MinecraftServer server, ServerPlayer player) implements SplitscreenPlayerInfo {
        @Override
        public Controller controller() {
            return this;
        }

        public GameProfile[] subPlayerProfiles() {
            return subPlayerProfiles;
        }

        public int subPlayerCount() {
            return subPlayerProfiles.length;
        }

        public List<ServerPlayer> subPlayers() {
            return Stream.of(subPlayerProfiles)
                    .map(profile -> server.getPlayerList().getPlayer(profile.getId()))
                    .toList();
        }

        public List<SubPlayer> subPlayerInfos() {
            return subPlayers().stream()
                    .map(SplitscreenPlayerInfo::get)
                    .map(opt -> (SubPlayer) opt.orElseThrow())
                    .toList();
        }

        @Override
        public boolean isController() {
            return true;
        }

        @Override
        public boolean isSubPlayer() {
            return false;
        }
    }

    record SubPlayer(GameProfile controllerProfile, SplitscreenServerSharedConfig sharedConfig, int pawnIndex, MinecraftServer server, ServerPlayer player) implements SplitscreenPlayerInfo {
        @Override
        public Controller controller() {
            return (Controller) get(server.getPlayerList().getPlayer(controllerProfile.getId())).orElseThrow();
        }

        @Override
        public boolean isController() {
            return false;
        }

        @Override
        public boolean isSubPlayer() {
            return true;
        }
    }

    interface SplitscreenPlayerInfoHolder {
        @Nullable SplitscreenPlayerInfo splitscreen$getPlayerInfo();
        void splitscreen$setPlayerInfo(SplitscreenPlayerInfo info);
    }
}
