package dev.isxander.controlify.mixins.feature.rumble.slowblock;

import com.mojang.authlib.GameProfile;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow
    protected abstract boolean isMoving();

    @Shadow
    public Input input;
    @Unique private ContinuousRumbleEffect slowBlockRumble = null;

    public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"))
    private void manageSlowBlockRumble(CallbackInfo ci) {
        float speed = this.getBlockSpeedFactor();

        if (speed < 1f && isMoving()) {
            ensureRumbleStarted();
        } else {
            ensureRumbleStopped();
        }
    }

    @Unique
    private void ensureRumbleStarted() {
        if (slowBlockRumble == null || slowBlockRumble.isFinished()) {
            slowBlockRumble = ContinuousRumbleEffect.builder()
                    .byTick(i -> {
                        float movementAmount = input.getMoveVector().length();
                        return new RumbleState(0.3f * movementAmount, 0.5f * movementAmount);
                    })
                    .timeout(100)
                    .build();
            ControlifyApi.get().getCurrentController()
                    .flatMap(ControllerEntity::rumble)
                    .ifPresent(rumble -> rumble.rumbleManager().play(
                            RumbleSource.PLAYER,
                            slowBlockRumble
                    ));
        } else {
            slowBlockRumble.heartbeat();
        }
    }

    @Unique
    private void ensureRumbleStopped() {
        if (slowBlockRumble != null && !slowBlockRumble.isFinished()) {
            slowBlockRumble.stop();
        }
    }
}
