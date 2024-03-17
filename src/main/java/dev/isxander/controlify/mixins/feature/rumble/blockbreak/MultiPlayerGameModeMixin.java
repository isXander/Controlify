package dev.isxander.controlify.mixins.feature.rumble.blockbreak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.utils.Easings;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Unique private ContinuousRumbleEffect blockBreakRumble = null;

    @Inject(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"))
    private void onStartBreakingBlock(BlockState state, BlockPos pos, Direction direction, int i, CallbackInfoReturnable<Packet<?>> cir) {
        startRumble(state);
    }

    @Inject(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"))
    private void onInstabreakBlockSurvival(BlockState state, BlockPos pos, Direction direction, int i, CallbackInfoReturnable<Packet> cir) {
        startRumble(state);
        // won't stop until 1 tick
        stopRumble();
    }

    @Inject(method = "stopDestroyBlock", at = @At("RETURN"))
    private void onStopBreakingBlock(CallbackInfo ci) {
        stopRumble();
    }

    @Inject(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void onFinishBreakingBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        stopRumble();
    }

    @ModifyExpressionValue(method = "continueDestroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean onAbortBreakingBlock(boolean original) {
        if (original)
            stopRumble();

        return original;
    }

    @Unique
    private void startRumble(BlockState state) {
        stopRumble();

        var effect = ContinuousRumbleEffect.builder()
                .byTick(tick -> new RumbleState(
                        0.02f + Easings.easeInQuad(Math.min(1, state.getBlock().defaultDestroyTime() / 20f)) * 0.25f,
                        0.01f
                ))
                .minTime(1)
                .build();

        blockBreakRumble = effect;
        ControlifyApi.get().getCurrentController()
                .flatMap(ControllerEntity::rumble)
                .ifPresent(rumble -> rumble.rumbleManager().play(RumbleSource.INTERACTION, effect));
    }

    @Unique
    private void stopRumble() {
        if (blockBreakRumble != null) {
            blockBreakRumble.stop();
            blockBreakRumble = null;
        }
    }
}
