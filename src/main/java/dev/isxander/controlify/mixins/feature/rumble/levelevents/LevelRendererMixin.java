package dev.isxander.controlify.mixins.feature.rumble.levelevents;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "levelEvent", at = @At("HEAD"))
    private void onLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        switch (eventId) {
            case LevelEvent.SOUND_ANVIL_USED -> rumble(
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
            case LevelEvent.SOUND_DRAGON_DEATH -> rumble(
                    RumbleSource.WORLD,
                    BasicRumbleEffect.join(
                            BasicRumbleEffect.constant(1f, 1f, 194),
                            BasicRumbleEffect.byTime(t -> {
                                float easeOutQuad = Easings.easeOutQuad(t);
                                return new RumbleState(1 - easeOutQuad, 1 - easeOutQuad);
                            }, 63)
                    ).prioritised(10)
            );
            case LevelEvent.SOUND_WITHER_BOSS_SPAWN -> rumble(
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

    @Unique
    private void rumble(RumbleSource source, RumbleEffect effect) {
        ControlifyApi.get().getCurrentController()
                .flatMap(ControllerEntity::rumble)
                .ifPresent(rumble -> rumble.rumbleManager().play(source, effect));
    }
}
