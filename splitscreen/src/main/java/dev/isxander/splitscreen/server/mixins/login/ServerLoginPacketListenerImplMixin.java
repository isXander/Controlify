package dev.isxander.splitscreen.server.mixins.login;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.isxander.splitscreen.server.login.LoginListenerStateHolder;
import dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerImplMixin implements LoginListenerStateHolder {

    @Shadow private @Nullable GameProfile authenticatedProfile;
    @Shadow @Final Connection connection;
    @Shadow @Final static Logger LOGGER;
    @Shadow @Nullable String requestedUsername;

    @Shadow protected abstract void finishLoginAndWaitForClient(GameProfile profile);
    @Shadow abstract void startClientVerification(GameProfile authenticatedProfile);


    @Unique private boolean firstFinishLoginPass = true;
    @Unique private boolean finishedFinishLogin = false;
    @Unique private boolean firstHelloPass = true;
    @Unique private boolean finishedHello = false;

    @Unique
    private final SplitscreenLoginFlowServer.ListenerState state = new SplitscreenLoginFlowServer.ListenerState();


    /**
     * Modifies the useAuthentication boolean when sending the hello packet.
     * Usually always true, sets to false if the client has already passed the splitscreen auth
     * so the client doesn't need to authenticate with Mojang.
     * @param shouldRequestMojangAuth the default constant: true
     * @return the modified value
     */
    @Definition(id = "ClientboundHelloPacket", type = ClientboundHelloPacket.class)
    @Expression("new ClientboundHelloPacket(?, ?, ?, @(true))")
    @ModifyExpressionValue(method = "handleHello", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean modifyShouldRequestMojangAuth(boolean shouldRequestMojangAuth) {
        return shouldRequestMojangAuth && !state.passedSplitscreenAuth();
    }

    /**
     * Injects into handleHello to start the splitscreen login flow.
     * @param packet
     * @param ci
     */
    @Inject(
            method = "handleHello",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;requestedUsername:Ljava/lang/String;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void onHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        if (!firstHelloPass) {
            return;
        }

        SplitscreenLoginFlowServer.startIdentifyFlow((ServerLoginPacketListenerImpl) (Object) this, this.connection, packet);

        // we inject before we send any packets or start any authentication processes
        // we don't want to prevent these, but we want to run it later (after splitscreen identity flow)
        // startIdentityFlow will call this method again within a packet receiver,
        // this field is here to prevent recursion
        firstHelloPass = false;
        ci.cancel();
    }

    /**
     * This prevents a crash because if the state is already HELLO, it will fail, but since
     * we run this method twice, we bypass this check.
     * @param isHello this.state == HELLO
     * @return true if the precondition should pass
     */
    @Definition(id = "HELLO", field = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;HELLO:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;")
    @Definition(id = "state", field = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;state:Lnet/minecraft/server/network/ServerLoginPacketListenerImpl$State;")
    @Expression("this.state == HELLO")
    @ModifyExpressionValue(method = "handleHello", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean shouldPassWithSecondHelloPass(boolean isHello) {
        if (!firstHelloPass && !finishedHello) {
            finishedHello = true;
            return true;
        } else {
            return isHello;
        }
    }

    /**
     * Delegate this until the splitscreen login flow is complete.
     * We re-run this in {@link #checkIfLoginCanComplete(CallbackInfo)} (on tick).
     * <p>
     * Wrapping the packet send operation rather than the method so the vanilla can set the state to PROTOCOL_SWITCHING
     * instead of potentially WAITING_FOR_DUPE_DISCONNECT which would cause a run every tick.
     * @param profile the profile associated with this login
     * @param original the original method body
     */
    @WrapOperation(method = "finishLoginAndWaitForClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onLoginComplete(Connection instance, Packet<?> packet, Operation<Void> original, @Local(argsOnly = true) GameProfile profile) {
        var targetThis = (ServerLoginPacketListenerImpl) (Object) this;
        if (firstFinishLoginPass && SplitscreenLoginFlowServer.onLoginComplete(targetThis, profile)) {
            firstFinishLoginPass = false;
        } else {
            finishedFinishLogin = true;
            original.call(instance, packet);
        }
    }

    /**
     * Check if we can finish the login process.
     */
    @Inject(method = "tick", at = @At("RETURN"))
    private void checkIfLoginCanComplete(CallbackInfo ci) {
        if (!finishedFinishLogin && !firstFinishLoginPass && state.canFinishLogin()) {
            finishLoginAndWaitForClient(authenticatedProfile);
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
        if (this.authenticatedProfile != null) {
            SplitscreenLoginFlowServer.onClientDisconnect(this.authenticatedProfile, details);
        }
    }

    /**
     * Even though we set usesAuthentication to false in the hello packet, the server still tries to authenticate
     * if encryption is enabled anyway.
     * This mixin prevents authentication running if we have passed splitscreen authentication.
     * @param thread the user authentication thread to start
     * @param original the original start() operation we have wrapped
     */
    @WrapOperation(method = "handleKey", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private void wrapUserAuthThreadStart(Thread thread, Operation<Void> original) {
        if (state.passedSplitscreenAuth()) {
            GameProfile profile = UUIDUtil.createOfflineProfile(Objects.requireNonNull(this.requestedUsername));
            LOGGER.info("Allowing splitscreen player {} to join with UUID {}", profile.name(), profile.id());
            startClientVerification(profile);
        } else {
            original.call(thread);
        }
    }

    @Override
    public SplitscreenLoginFlowServer.ListenerState splitscreen$state() {
        return state;
    }

}
