package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.RumbleEffect;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin {
    @Override
    protected void onUpdateUsingItem(ItemStack stack, CallbackInfo ci) {
        switch (stack.getUseAnimation()) {
            case BOW, CROSSBOW, SPEAR -> {
                var magnitude = Mth.clamp((stack.getUseDuration() - getUseItemRemainingTicks()) / 20f, 0f, 1f) * 0.5f;
                playRumble(RumbleEffect.constant(magnitude * 0.3f, magnitude, 1));
            }
            case BLOCK, SPYGLASS -> playRumble(RumbleEffect.constant(0f, 0.1f, 1));
            case EAT, DRINK -> playRumble(RumbleEffect.constant(0.05f, 0.1f, 1));
            case TOOT_HORN -> playRumble(RumbleEffect.constant(1f, 0.25f, 1));
        }
    }

    private void playRumble(RumbleEffect effect) {
        ControlifyApi.get().currentController().rumbleManager().play(effect);
    }
}
