package dev.isxander.controlify.mixins.feature.rumble.land;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.rumble.RumbleEffect;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends EntityMixin {
    @Override
    protected void onLand(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
        Controlify.instance().currentController().rumbleManager().play(RumbleEffect.constant(0f, 0.25f, 1));
    }
}
