package dev.isxander.splitscreen.client.mixins.core;

import com.mojang.datafixers.DataFixer;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin extends MinecraftServer {
    public IntegratedServerMixin(Thread serverThread, LevelStorageSource.LevelStorageAccess storageSource, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer fixerUpper, Services services, LevelLoadListener levelLoadListener) {
        super(serverThread, storageSource, packRepository, worldStem, proxy, fixerUpper, services, levelLoadListener);
    }

    /**
     * In {@link MinecraftMixin#shouldTickServerInPausableScreen(boolean)} we allow the game to pause
     * even when open to LAN. In vanilla, the connection is not ticked which means the other clients cannot
     * connect to the server.
     */
    @Inject(method = "tickPaused", at = @At("HEAD"))
    private void tickConnectionWhenPaused(CallbackInfo ci) {
        if (SplitscreenBootstrapper.isSplitscreen()) {
            this.tickConnection();
        }
    }
}
