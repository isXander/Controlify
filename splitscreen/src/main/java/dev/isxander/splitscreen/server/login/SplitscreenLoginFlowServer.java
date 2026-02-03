package dev.isxander.splitscreen.server.login;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import dev.isxander.splitscreen.config.SplitscreenServerConfig;
import dev.isxander.splitscreen.config.SplitscreenServerSharedConfig;
import dev.isxander.splitscreen.server.mixins.login.ServerLoginPacketListenerImplMixin;
import dev.isxander.splitscreen.server.login.packets.ClientboundIdentifyPacket;
import dev.isxander.splitscreen.server.login.packets.ClientboundNoncePacket;
import dev.isxander.splitscreen.server.login.packets.ServerboundIdentifyPacket;
import dev.isxander.splitscreen.server.status.ServerStatusSplitscreenExt;
import dev.isxander.splitscreen.util.CSUtil;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SplitscreenLoginFlowServer {
    public static final int PROTOCOL_VERSION = 1;
    public static final Identifier CHANNEL_IDENTIFY = CSUtil.rl("splitscreen_identify");
    public static final Identifier CHANNEL_CONTROLLER = CSUtil.rl("splitscreen_controller");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new SecureRandom();

    // map of primary uuid to state
    private static final Map<UUID, ControllerState> CONTROLLER_STATE = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> SUB_PLAYER_TO_CONTROLLER = new ConcurrentHashMap<>();

    public static void init() {
    }

    /**
     * Called from {@link ServerLoginPacketListenerImplMixin#onHello(ServerboundHelloPacket, CallbackInfo)},
     * this method initiates the login flow for splitscreen.
     * It's responsible for sending the identify packet to the client and waiting for the client to respond with:
     * <ul>
     *     <li>I'm a controller</li>
     *     <li>I'm a pawn</li>
     *     <li>I do not understand this packet (vanilla/non splitscreen client)</li>
     * </ul>
     * @param listener0 the listener associated with this login
     * @param connection the connection associated with this player
     * @param helloPacket the hello packet send by the client to initiate the login
     */
    public static void startIdentifyFlow(ServerLoginPacketListenerImpl listener0, Connection connection, ServerboundHelloPacket helloPacket) {
        // crude impl way to send packets outside of listeners and FAPI events
        LoginPacketSender sender0 = ServerLoginNetworking.getSender(listener0);

        // send that identify packet
        sender0.sendPacket(CHANNEL_IDENTIFY, new ClientboundIdentifyPacket(PROTOCOL_VERSION).encode());
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
                switch (packet.identification()) {
                    case ClientIdentification.Pawn(UUID controllerUuid, byte[] hmac, int subPlayerIndex) -> {
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
                            if (!SplitscreenServerConfig.INSTANCE.allowLateLogins.get() && server.isDedicatedServer()) {
                                LOGGER.error("Pawn has attempted to log in late but it is not allowed on this server.");
                                listener.disconnect(Component.translatable("controlify.splitscreen.login.late_login_not_allowed"));
                                return;
                            } else if (!controllerState.isNextSubPlayerIndex(subPlayerIndex)) {
                                LOGGER.error("Pawn that is logging in late does not have the sequential sub player index. Expected {} got {}", controllerState.subPlayers, subPlayerIndex);
                                listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_subplayer_index"));
                                return;
                            } else {
                                LOGGER.warn("Allowing pawn to log in late.");
                            }
                        } else {
                            if (!controllerState.isValidSubPlayerIndex(subPlayerIndex)) {
                                // don't allow sub-players with out-of-range indexes to what the controller has requested
                                // e.g. don't accept Player10 if the controller has only requested 4 players
                                LOGGER.error("Pawn has sent an invalid sub-player index. {} is not in range 0-{}", subPlayerIndex, controllerState.subPlayers());
                                listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_subplayer_index"));
                                return;
                            }

                            // if the player index is in range, but we're not waiting for it, it's already logged in
                            // we could rely on the vanilla flow to detect this, but I'd rather be explicit, and that
                            // would disconnect the existing player, not the currently logging in one
                            if (!controllerState.isWaitingForSubPlayer(subPlayerIndex)) {
                                LOGGER.error("Pawn has attempted to log in with a sub-player index {} that is already logged in.", subPlayerIndex);
                                listener.disconnect(Component.translatable("controlify.splitscreen.login.subplayer_already_logged_in"));
                                return;
                            }
                        }

                        // enforce the username of the sub-player so they can't impersonate other players
                        // and are clearly associated with the main player.
                        GameProfile subPlayerProfile = controllerState.subPlayerProfile(subPlayerIndex);
                        if (!SplitscreenServerConfig.INSTANCE.allowAnyUsername.get() && !Objects.equals(subPlayerProfile.name(), helloPacket.name())) {
                            LOGGER.error("Pawn has sent an invalid username. {} is not the expected username {}", helloPacket.name(), subPlayerProfile.name());
                            listener.disconnect(Component.translatable("controlify.splitscreen.login.invalid_profile"));
                            return;
                        }

                        SUB_PLAYER_TO_CONTROLLER.put(subPlayerProfile.id(), controllerUuid);
                        controllerState.signalSubPlayerFinished(subPlayerIndex, subPlayerProfile, connection);
                        listenerState.passedSplitscreenAuth = true;
                        listenerState.controllerState = controllerState;
                    }
                    case ClientIdentification.Controller(int subPlayerCount, SplitscreenServerSharedConfig config) -> {
                        // we have to enforce the maximum amount of clients that our nonce is allowed to spawn in.
                        int maxClients = SplitscreenServerConfig.INSTANCE.maxClients.get();
                        if (subPlayerCount > maxClients) {
                            LOGGER.error("Controller has requested too many clients. {} is greater than server-defined limit of {}", subPlayerCount, maxClients);
                            listener.disconnect(Component.translatable("controlify.splitscreen.login.too_many_requested_clients", maxClients + 1));
                            return;
                        }
                    }
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
     * Called from {@link ServerLoginPacketListenerImplMixin#onLoginComplete(GameProfile, CallbackInfo)}
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
        LoginPacketSender sender = ServerLoginNetworking.getSender(listener);

        LOGGER.info("Saved client identification {}", identification);

        if (identification instanceof ClientIdentification.Controller(int subPlayerCount, SplitscreenServerSharedConfig config)) {
            // At this point in the login flow (if online mode is enabled) this client has been authenticated
            // fully with their Minecraft account, and encryption has been enabled if applicable.
            // This means we can safely send the nonce to the client securely.

            byte[] nonce = generateNonce();
            var controllerState = new ControllerState(profile, nonce, subPlayerCount, config);
            CONTROLLER_STATE.put(profile.id(), controllerState);
            state.controllerState = controllerState;

            LOGGER.info("Sending nonce to controller {}", profile.name());
            sender.sendPacket(CHANNEL_CONTROLLER, new ClientboundNoncePacket(nonce).encode());

            ServerLoginNetworking.registerReceiver(listener, CHANNEL_CONTROLLER, (server, listener1, understood, buf, synchronizer, sender1) -> {
                state.nonceAck |= understood;
                if (subPlayerCount == 0) {
                    controllerState.allDone().complete(null);
                }
            });
        }

        if (state.controllerState != null) {
            // do not switch to configuration until all clients have logged in.
            LOGGER.info("Delaying login for controller {}: waiting for sub-players", profile.name());
            return true;
        }

        return false;
    }

    public static void onClientDisconnect(GameProfile clientProfile, DisconnectionDetails disconnectionDetails) {
        ControllerState state = state(clientProfile.id());
        if (state != null) {
            // propagate the disconnection to all sub-players
            for (WeakReference<Connection> connectionRef : state.subPlayerConnections.values()) {
                Connection connection = connectionRef.get();
                if (connection != null) {
                    connection.disconnect(disconnectionDetails);
                }
            }

            // remove the controller state from the map
            CONTROLLER_STATE.remove(clientProfile.id());
        }
    }

    public static ServerStatusSplitscreenExt buildSplitscreenStatus() {
        int maxClients = SplitscreenServerConfig.INSTANCE.maxClients.get();
        return new ServerStatusSplitscreenExt(new int[]{PROTOCOL_VERSION}, maxClients);
    }

    public static ListenerState state(ServerLoginPacketListenerImpl listener) {
        return ((LoginListenerStateHolder) listener).splitscreen$state();
    }

    private static @Nullable ControllerState state(UUID uuid) {
        return CONTROLLER_STATE.get(uuid);
    }

    public static @Nullable ControllerState getStateFromControllerOrSubplayer(UUID uuid) {
        // check if the uuid is a controller
        ControllerState controllerState = CONTROLLER_STATE.get(uuid);
        if (controllerState != null) {
            return controllerState;
        }

        // check if the uuid is a sub-player
        UUID controllerUuid = SUB_PLAYER_TO_CONTROLLER.get(uuid);
        if (controllerUuid != null) {
            return CONTROLLER_STATE.get(controllerUuid);
        }

        return null;
    }

    private static byte[] generateNonce() {
        byte[] nonce = new byte[ClientboundNoncePacket.NONCE_SIZE_BYTES];
        RANDOM.nextBytes(nonce);
        return nonce;
    }

    static byte[] generateHmac(byte @Nullable [] nonce, UUID controllerUuid, int subPlayerIndex) {
        ByteBuffer hmacBuf = ByteBuffer.allocate(Long.BYTES * 2 + Integer.BYTES);
        hmacBuf.putLong(controllerUuid.getMostSignificantBits());
        hmacBuf.putLong(controllerUuid.getLeastSignificantBits());
        hmacBuf.putInt(subPlayerIndex);
        hmacBuf.flip();

        if (nonce == null) {
            LOGGER.warn("Generating HMAC with random nonce, since none was provided. Server will probably deny login.");
            nonce = generateNonce();
        }

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
            return controllerState.allDone().isDone() &&
                    (!(identification instanceof ClientIdentification.Controller) || nonceAck);
        }
    }

    public static class ControllerState {
        private final GameProfile hostProfile;
        private final byte[] nonce;
        private final int subPlayers;
        private final Set<Integer> subPlayerWaiting = ConcurrentHashMap.newKeySet();
        private final CompletableFuture<Void> allDoneFuture = new CompletableFuture<>();
        private final Map<UUID, WeakReference<Connection>> subPlayerConnections = new ConcurrentHashMap<>();
        private final GameProfile[] subPlayerProfiles;
        private final SplitscreenServerSharedConfig sharedConfig;

        private ControllerState(GameProfile hostProfile, byte[] nonce, int subPlayers, SplitscreenServerSharedConfig sharedConfig) {
            this.hostProfile = hostProfile;
            this.nonce = nonce;
            this.subPlayers = subPlayers;
            for (int i = 0; i < subPlayers; i++) {
                subPlayerWaiting.add(i);
            }
            this.subPlayerProfiles = new GameProfile[subPlayers];
            this.sharedConfig = sharedConfig;
        }

        public GameProfile hostProfile() {
            return hostProfile;
        }

        public GameProfile subPlayerProfile(int index) {
            // uses the `.` to make sure a splitscreen player can never share a username with an actual authenticated player
            String username = hostProfile.name() + "." + (index + 1);
            // uuid should be deterministic from the *account*, not their current username.
            // this prevents the main player's username from changing the sub-player's uuid
            // this comes with the side effect that the load order of the sub-player determines their uuid
            // which may not be ideal
            UUID uuid = UUID.nameUUIDFromBytes((this.hostProfile.id() + "splitscreen" + index).getBytes(StandardCharsets.UTF_8));

            return new GameProfile(uuid, username);
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

        public boolean isNextSubPlayerIndex(int index) {
            return index == subPlayers;
        }

        public boolean isWaitingForSubPlayer(int index) {
            return subPlayerWaiting.contains(index);
        }

        public int getSubPlayerIndex(GameProfile profile) {
            for (int i = 0; i < subPlayerProfiles().length; i++) {
                if (subPlayerProfiles()[i].equals(profile)) {
                    return i;
                }
            }
            return -1;
        }

        public void signalSubPlayerFinished(int index, GameProfile profile, Connection connection) {
            if (subPlayerWaiting.remove(index)) {
                subPlayerConnections.put(profile.id(), new WeakReference<>(connection));
                subPlayerProfiles[index] = profile;
                if (subPlayerWaiting.isEmpty()) {
                    allDoneFuture.complete(null);
                }
            } else if (!allDone().isDone()) {
                throw new IllegalStateException("Subplayer " + index + " was not waiting. Duplicate login?");
            }
        }

        public GameProfile[] subPlayerProfiles() {
            return subPlayerProfiles;
        }

        public Connection subPlayerConnection(UUID uuid) {
            WeakReference<Connection> connection = subPlayerConnections.get(uuid);
            if (connection != null) {
                return connection.get();
            } else {
                return null;
            }
        }

        public SplitscreenServerSharedConfig sharedConfig() {
            return sharedConfig;
        }
    }
}
