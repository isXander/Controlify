package dev.isxander.splitscreen.server.login;

/**
 * Duck interface that holds {@link dev.isxander.splitscreen.server.login.SplitscreenLoginFlowServer.ListenerState}
 * within {@link net.minecraft.server.network.ServerLoginPacketListenerImpl}.
 */
public interface LoginListenerStateHolder {
    SplitscreenLoginFlowServer.ListenerState splitscreen$state();
}
