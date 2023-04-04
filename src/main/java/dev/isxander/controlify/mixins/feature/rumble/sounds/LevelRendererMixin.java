package dev.isxander.controlify.mixins.feature.rumble.sounds;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void onLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        switch (eventId) {
            case LevelEvent.SOUND_ANVIL_USED -> rumble(
                    RumbleEffect.join(
                            RumbleEffect.constant(1f, 0.5f, 2),
                            RumbleEffect.empty(5)
                    ).repeat(3)
            );
        }
    }

    @Inject(method = "globalLevelEvent", at = @At("HEAD"))
    private void onGlobalLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        switch (eventId) {
            case LevelEvent.SOUND_DRAGON_DEATH -> rumble(
                    RumbleEffect.join(
                            RumbleEffect.constant(1f, 1f, 194),
                            RumbleEffect.byTime(t -> {
                                float easeOutQuad = 1 - (1 - t) * (1 - t);
                                return new RumbleState(1 - easeOutQuad, 1 - easeOutQuad);
                            }, 63)
                    )
            );
        }
    }

    private void rumble(RumbleEffect effect) {
        ControlifyApi.get().currentController().rumbleManager().play(effect);
    }
}
