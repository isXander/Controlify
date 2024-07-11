package dev.isxander.controlify.platform.main.events;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface HandshakeCompletionEvent<I> {
    void onCompletion(@Nullable I packet, ServerLoginPacketListenerImpl handler);
}
