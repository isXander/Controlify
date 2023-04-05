package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.rumble.BasicRumbleEffect;
import dev.isxander.controlify.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin {
    @Unique private ContinuousRumbleEffect useItemRumble;

    @Override
    protected void onStartUsingItem(InteractionHand hand, CallbackInfo ci, ItemStack stack) {
        switch (stack.getUseAnimation()) {
            case BOW, CROSSBOW, SPEAR ->
                    startRumble(new ContinuousRumbleEffect(tick ->
                            new RumbleState(tick % 7 <= 3 && tick > BowItem.MAX_DRAW_DURATION ? 0.1f : 0f, BowItem.getPowerForTime(tick))
                    ));
            case BLOCK, SPYGLASS ->
                    startRumble(new ContinuousRumbleEffect(tick ->
                            new RumbleState(0f, tick % 4 / 4f * 0.12f + 0.05f)
                    ));
            case EAT, DRINK ->
                    startRumble(new ContinuousRumbleEffect(tick ->
                            new RumbleState(0.05f, 0.1f)
                    ));
            case TOOT_HORN ->
                    startRumble(new ContinuousRumbleEffect(tick ->
                            new RumbleState(Math.min(1f, tick / 10f), 0.25f)
                    ));
        }
    }

    @Override
    protected void onUpdateUsingItem(ItemStack stack, CallbackInfo ci) {

    }

    @Override
    protected void onStopUsingItem(CallbackInfo ci) {
        if (useItemRumble != null) {
            useItemRumble.stop();
            useItemRumble = null;
        }
    }

    private void startRumble(ContinuousRumbleEffect effect) {
        ControlifyApi.get().currentController().rumbleManager().play(RumbleSource.USE_ITEM, effect);
        useItemRumble = effect;
    }
}
