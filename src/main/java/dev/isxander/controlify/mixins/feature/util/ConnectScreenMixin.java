package dev.isxander.controlify.mixins.feature.util;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(method = "connect", at = @At("HEAD"))
    private void onConnect(
            Minecraft minecraft,
            ServerAddress hostAndPort,
            @Nullable ServerData server,
            @Nullable TransferState transferState,
            CallbackInfo ci
    ) {
        Controlify.instance().notifyNewServer(server);
    }
}
