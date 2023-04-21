package dev.isxander.controlify.mixins.feature.rumble.itembreak;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.RumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends LivingEntityMixin {
    @Override
    protected void onBreakItemParticles(ItemStack stack, CallbackInfo ci) {
        ControlifyApi.get().getCurrentController().ifPresent(controller -> controller.rumbleManager().play(
                RumbleSource.ITEM_BREAK,
                BasicRumbleEffect.byTick(tick -> new RumbleState(tick <= 4 ? 1f : 0f, 1f), 10)
        ));
    }
}
