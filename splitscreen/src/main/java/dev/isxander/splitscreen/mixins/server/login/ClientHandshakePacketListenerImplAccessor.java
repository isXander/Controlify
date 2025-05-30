package dev.isxander.splitscreen.mixins.server.login;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientHandshakePacketListenerImpl.class)
public interface ClientHandshakePacketListenerImplAccessor {
    @Accessor
    ServerData getServerData();
}
