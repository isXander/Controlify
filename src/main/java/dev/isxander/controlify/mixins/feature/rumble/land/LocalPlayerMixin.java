package dev.isxander.controlify.mixins.feature.rumble.land;

import dev.isxander.controlify.Controlify;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends EntityMixin {
    @Override
    protected void onLand(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
        boolean rumbled = Controlify.instance().currentController().rumble(1f, 1f, 1000);
        System.out.println(rumbled);
    }
}
