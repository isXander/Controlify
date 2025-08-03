package dev.isxander.controlify.mixins.feature.rumble.levelevents;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        //? if >=1.21.2 {
        LevelEventHandler.class
        //?} else {
        /*LevelRenderer.class
        *///?}
)
public class LevelEventHandlerMixin {
    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void onLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        switch (eventId) {
            case LevelEvent.SOUND_ANVIL_USED -> ControlifyApi.get().playRumbleEffect(
                    RumbleSource.GUI,
                    BasicRumbleEffect.join(
                            BasicRumbleEffect.constant(1f, 0.5f, 2),
                            BasicRumbleEffect.empty(5)
                    ).repeat(3)
            );
        }
    }

    @Inject(method = "globalLevelEvent", at = @At("HEAD"))
    private void onGlobalLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        switch (eventId) {
            case LevelEvent.SOUND_DRAGON_DEATH -> ControlifyApi.get().playRumbleEffect(
                    RumbleSource.WORLD,
                    BasicRumbleEffect.join(
                            BasicRumbleEffect.constant(1f, 1f, 194),
                            BasicRumbleEffect.byTime(t -> {
                                float easeOutQuad = (float) Easings.easeOutQuad(t);
                                return new RumbleState(1 - easeOutQuad, 1 - easeOutQuad);
                            }, 63)
                    ).prioritised(10)
            );
            case LevelEvent.SOUND_WITHER_BOSS_SPAWN -> ControlifyApi.get().playRumbleEffect(
                    RumbleSource.WORLD,
                    BasicRumbleEffect.join(
                            BasicRumbleEffect.constant(1f, 1f, 9),
                            BasicRumbleEffect.constant(0.1f, 1f, 14),
                            BasicRumbleEffect.byTime(t -> {
                                float easeOutQuad = 1 - (1 - t) * (1 - t);
                                return new RumbleState(0f, 1 - easeOutQuad);
                            }, 56)
                    ).prioritised(10)
            );
        }
    }
}
