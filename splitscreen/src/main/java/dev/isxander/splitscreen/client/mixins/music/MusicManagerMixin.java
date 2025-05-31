package dev.isxander.splitscreen.client.mixins.music;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    /**
     * Don't allow music on any client other than the primary one,
     * since these tracks will conflict and sound awful.
     */
    @WrapMethod(method = "startPlaying")
    private void preventMusicIfPawn(MusicInfo music, Operation<Void> original) {
        SplitscreenBootstrapper.getControllerBridge().ifPresentOrElse(
                bridge -> bridge.requestPlayMusic(music.music(), music.volume()),
                () -> original.call(music)
        );
    }
}
