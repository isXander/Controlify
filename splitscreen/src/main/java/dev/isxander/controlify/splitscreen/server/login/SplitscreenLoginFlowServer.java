package dev.isxander.controlify.splitscreen.server.login;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.isxander.controlify.splitscreen.server.login.packets.ClientboundIdentifyPacket;
import dev.isxander.controlify.splitscreen.server.login.packets.ClientboundNoncePacket;
import dev.isxander.controlify.splitscreen.server.login.packets.ServerboundIdentifyPacket;
import dev.isxander.controlify.splitscreen.util.CSUtil;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// TODO: disconnect sub-players if the main player disconnects
// TODO: formats for usernames, negotiated via the Nonce packet
public class SplitscreenLoginFlowServer {
    public static final ResourceLocation CHANNEL_IDENTIFY = CSUtil.rl("splitscreen_identify");
    public static final ResourceLocation CHANNEL_CONTROLLER = CSUtil.rl("splitscreen_controller");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new SecureRandom();

    // map of primary uuid to state
    // TODO: remove from this map as players log out
    private static final Map<UUID, ControllerState> CONTROLLER_STATE = new ConcurrentHashMap<>();

    public static void init() {
    }

    /**
     * Called from {@link dev.isxander.controlify.splitscreen.mixins.server.login.ServerLoginPacketListenerImplMixin#onHello(ServerboundHelloPacket, CallbackInfo)},
     * this method initiates the login flow for splitscreen.
     * It's responsible for sending the identify packet to the client and waiting for the client to respond with:
     * - "I'm a controller"
     * - "I'm a pawn"
     * - "I do not understand this packet" (vanilla/non splitscreen client)
     * @param listener0 the listener associated with this login
     * @param connection the connection associated with this player
     * @param helloPacket the hello packet send by the client to initiate the login
     */
    public static void startIdentifyFlow(ServerLoginPacketListenerImpl listener0, Connection connection, ServerboundHelloPacket helloPacket) {
        // crude impl way to send packets outside of listeners and FAPI events
        LoginPacketSender sender0 = ServerNetworkingImpl.getAddon(listener0);

        // send that identify packet
        sender0.sendPacket(CHANNEL_IDENTIFY, new ClientboundIdentifyPacket().encode());
        // listen for a response for the client
        ServerLoginNetworking.registerReceiver(listener0, CHANNEL_IDENTIFY, (server, listener, understood, buf, synchronizer, sender) -> {
            try { // to ensure that early returns and exceptions don't leave the channel open
                // clients respond to packets they don't understand with a null payload
                // this allows us to continue regular login without waiting for timeout
                if (!understood) {
                    LOGGER.info("Client is not a splitscreen client, continuing with vanilla login.");
                    return;
                }

                // this may throw, in which case it will disconnect.
                ServerboundIdentifyPacket packet = ServerboundIdentifyPacket.STREAM_CODEC.decode(buf);

                // create the state implicitly and set the identification
                state(listener).identification = packet.identification();

                // controllers are given their nonce *after* mojang authentication completes successfully
                // for pawns we need to skip mojang authentication, so we do it immediately.
                if (packet.identification() instanceof ClientIdentification.Pawn(UUID controllerUuid, byte[] hmac, int subPlayerIndex)) {
                    LOGGER.info("Client has identified as a pawn.");
                    @Nullable ControllerState controllerState = state(controllerUuid);
                    ListenerState listenerState = state(listener);

                    if (controllerState == null) {
                        LOGGER.error("Client has identified as a pawn for a controller that is not attempting log in.");
                        listener.disconnect(Component.translatable("controlify.splitscreen.login.controller_not_found"));
                        return;
                    }

                    // regenerate the hmac the client has generated from the nonce and compare
                    // we do it this way since encryption has not yet been enabled in the protocol, and we don't
                    // want the nonce to be intercepted
                    byte[] expectedHmac = generateHmac(controllerState.nonce, controllerUuid, subPlayerIndex);
//                    LOGGER.info("Server HMAC: controller UUID {}, sub-player index {}, nonce {}", controllerUuid, subPlayerIndex, Hex.encodeHexString(controllerState.nonce));
                    if (!Arrays.equals(expectedHmac, hmac)) {
                        LOGGER.error("Pawn has sent an incorrect HMAC.");
                        listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_hmac"));
                        return;
                    }

                    if (controllerState.allDone().isDone()) {
                        // if the clients have already been logged in, we need to check if we allow late logins
                        if (!SplitscreenLoginConfig.ALLOW_LATE_LOGINS && server.isDedicatedServer()) {
                            LOGGER.error("Pawn has attempted to log in late but it is not allowed on this server.");
                            listener.disconnect(Component.translatable("controlify.splitscreen.login.late_login_not_allowed"));
                            return;
                        } else {
                            LOGGER.warn("Allowing pawn to log in late.");
                        }
                    } else if (!controllerState.isValidSubPlayerIndex(subPlayerIndex)) {
                        // don't allow sub-players with out-of-range indexes to what the controller has requested
                        // e.g. don't accept Player10 if the controller has only requested 4 players
                        LOGGER.error("Pawn has sent an invalid sub-player index. {} is not in range 0-{}", subPlayerIndex, controllerState.subPlayers());
                        listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_subplayer_index"));
                        return;
                    }

                    // enforce the username of the sub-player so they can't impersonate other players
                    // and are clearly associated with the main player.
                    GameProfile subPlayerProfile = controllerState.subPlayerProfile(subPlayerIndex);
                    if (!SplitscreenLoginConfig.ALLOW_ANY_USERNAME && !Objects.equals(subPlayerProfile.getName(), helloPacket.name())) {
                        LOGGER.error("Pawn has sent an invalid username. {} is not the expected username {}", helloPacket.name(), subPlayerProfile.getName());
                        listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_profile"));
                        return;
                    }

                    // if the player index is in range, but we're not waiting for it, it's already logged in
                    // we could rely on the vanilla flow to detect this, but I'd rather be explicit, and that
                    // would disconnect the existing player, not the currently logging in one
                    if (!controllerState.isWaitingForSubPlayer(subPlayerIndex)) {
                        LOGGER.error("Pawn has attempted to log in with a sub-player index that is already logged in.");
                        listener.disconnect(Component.translatable("controlify.splitscreen.login.subplayer_already_logged_in"));
                        return;
                    }

                    controllerState.signalSubPlayerFinished(subPlayerIndex, subPlayerProfile.getId(), connection);
                    listenerState.passedSplitscreenAuth = true;
                    listenerState.controllerState = controllerState;
                }
            } finally {
                // don't accept any more packets on this channel
                // default behaviour is for the server to disconnect the client
                // if the client attempts to send a packet on a channel it doesn't understand
                ServerLoginNetworking.unregisterReceiver(listener, CHANNEL_IDENTIFY);

                // this method is called from the handleHello method,
                // this may look recursive, but the mixin handles cancelling the first call
                // and allowing the second call to proceed with regular flow without rewriting
                // large sections of vanilla login flow
                listener.handleHello(helloPacket);
            }
        });
    }

    /**
     * Called from {@link dev.isxander.controlify.splitscreen.mixins.server.login.ServerLoginPacketListenerImplMixin#onLoginComplete(GameProfile, CallbackInfo)}
     * when the login phase is complete, just before switching to configuration phase.
     * Responsible for handing out nonces to controllers, and waiting for all sub-players to log in before proceeding.
     *
     * @param listener the listener associated with this login
     * @param profile the profile of the player logging in
     * @return true if we should cancel the protocol change
     */
    public static boolean onLoginComplete(ServerLoginPacketListenerImpl listener, GameProfile profile) {
        ListenerState state = state(listener);
        @Nullable ClientIdentification identification = state.identification;
        LoginPacketSender sender = ServerNetworkingImpl.getAddon(listener);

        LOGGER.info("Saved client identification {}", identification);

        if (identification instanceof ClientIdentification.Controller(int subPlayerCount)) {
            // we have to enforce the maximum amount of clients that our nonce is allowed to spawn in.
            // TODO: potentially give out a one-time-use nonce for each sub-player requested, but this prevents late logins completely
            if (subPlayerCount > SplitscreenLoginConfig.MAX_CLIENTS) {
                LOGGER.error("Controller has requested too many clients. {} is greater than server-defined limit of {}", subPlayerCount, SplitscreenLoginConfig.MAX_CLIENTS);
                listener.disconnect(Component.translatable("controlify.splitscreen.login.too_many_requested_clients", SplitscreenLoginConfig.MAX_CLIENTS + 1));
                return true;
            }

            // At this point in the login flow (if online mode is enabled) this client has been authenticated
            // fully with their Minecraft account, and encryption has been enabled if applicable.
            // This means we can safely send the nonce to the client securely.

            byte[] nonce = generateNonce();
            var controllerState = new ControllerState(profile, nonce, subPlayerCount);
            CONTROLLER_STATE.put(profile.getId(), controllerState);
            state.controllerState = controllerState;

            LOGGER.info("Sending nonce to controller {}", profile.getName());
            sender.sendPacket(CHANNEL_CONTROLLER, new ClientboundNoncePacket(nonce).encode());

            ServerLoginNetworking.registerReceiver(listener, CHANNEL_CONTROLLER, (server, listener1, understood, buf, synchronizer, sender1) -> {
                state.nonceAck |= understood;
            });
        }

        if (state.controllerState != null) {
            // do not switch to configuration until all clients have logged in.
            LOGGER.info("Delaying login for controller {}: waiting for sub-players", profile.getName());
            return true;
        }

        return false;
    }

    public static void onClientDisconnect(GameProfile clientProfile, DisconnectionDetails disconnectionDetails) {
        ControllerState state = state(clientProfile.getId());
        if (state != null) {
            // propagate the disconnection to all sub-players
            for (WeakReference<Connection> connectionRef : state.subPlayerConnections.values()) {
                Connection connection = connectionRef.get();
                if (connection != null) {
                    connection.disconnect(disconnectionDetails);
                }
            }

            // remove the controller state from the map
            CONTROLLER_STATE.remove(clientProfile.getId());
        }
    }

    public static ListenerState state(ServerLoginPacketListenerImpl listener) {
        return ((LoginListenerStateHolder) listener).splitscreen$state();
    }

    private static @Nullable ControllerState state(UUID uuid) {
        return CONTROLLER_STATE.get(uuid);
    }

    private static byte[] generateNonce() {
        byte[] nonce = new byte[ClientboundNoncePacket.NONCE_SIZE_BYTES];
        RANDOM.nextBytes(nonce);
        return nonce;
    }

    static byte[] generateHmac(byte[] nonce, UUID controllerUuid, int subPlayerIndex) {
        ByteBuffer hmacBuf = ByteBuffer.allocate(Long.BYTES * 2 + Integer.BYTES);
        hmacBuf.putLong(controllerUuid.getMostSignificantBits());
        hmacBuf.putLong(controllerUuid.getLeastSignificantBits());
        hmacBuf.putInt(subPlayerIndex);
        hmacBuf.flip();

        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, nonce).hmac(hmacBuf);
    }

    public static class ListenerState {
        private ControllerState controllerState;
        private @Nullable ClientIdentification identification;
        // allows the server to bypass mojang authentication
        private boolean passedSplitscreenAuth = false;
        private boolean nonceAck;

        public boolean passedSplitscreenAuth() {
            return passedSplitscreenAuth;
        }

        public @Nullable ControllerState controllerState() {
            return controllerState;
        }

        public boolean hasAckedNonce() {
            return nonceAck;
        }

        public boolean canFinishLogin() {
            return (identification instanceof ClientIdentification.Controller && nonceAck) ||
                    controllerState.allDone().isDone();
        }
    }

    public static class ControllerState {
        private final GameProfile hostProfile;
        private final byte[] nonce;
        private final int subPlayers;
        private final Set<Integer> subPlayerWaiting = ConcurrentHashMap.newKeySet();
        private final CompletableFuture<Void> allDoneFuture = new CompletableFuture<>();
        private final Map<UUID, WeakReference<Connection>> subPlayerConnections = new ConcurrentHashMap<>();

        private ControllerState(GameProfile hostProfile, byte[] nonce, int subPlayers) {
            this.hostProfile = hostProfile;
            this.nonce = nonce;
            this.subPlayers = subPlayers;
            for (int i = 0; i < subPlayers; i++) {
                subPlayerWaiting.add(i);
            }
        }

        public GameProfile hostProfile() {
            return hostProfile;
        }

        public GameProfile subPlayerProfile(int index) {
            // TODO: figure out an invalid-but-parsable format so we don't conflict with other players on the server.
            // TODO: alternatively, prioritise the existing real player over the subplayer (don't disconnect them)
            return UUIDUtil.createOfflineProfile(hostProfile().getName() + (index + 1));
        }

        public CompletableFuture<Void> allDone() {
            return allDoneFuture;
        }

        public byte[] nonce() {
            return nonce;
        }

        public int subPlayers() {
            return subPlayers;
        }

        public boolean isValidSubPlayerIndex(int index) {
            return index >= 0 && index < subPlayers;
        }

        public boolean isWaitingForSubPlayer(int index) {
            return subPlayerWaiting.contains(index);
        }

        public void signalSubPlayerFinished(int index, UUID uuid, Connection connection) {
            if (subPlayerWaiting.remove(index)) {
                subPlayerConnections.put(uuid, new WeakReference<>(connection));
                if (subPlayerWaiting.isEmpty()) {
                    allDoneFuture.complete(null);
                }
            } else {
                throw new IllegalStateException("Subplayer " + index + " was not waiting. Duplicate login?");
            }
        }

        public Connection subPlayerConnection(UUID uuid) {
            WeakReference<Connection> connection = subPlayerConnections.get(uuid);
            if (connection != null) {
                return connection.get();
            } else {
                return null;
            }
        }
    }
}
