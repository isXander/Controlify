package dev.isxander.controlify.platform.main.events;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;

@FunctionalInterface
public interface HandshakeCompletionEvent<I> {
    void onCompletion(I packet, boolean understood, ServerLoginPacketListenerImpl handler);
}
