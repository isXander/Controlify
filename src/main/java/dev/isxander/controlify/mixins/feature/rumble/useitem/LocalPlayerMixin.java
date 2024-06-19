package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.rumble.*;
import dev.isxander.controlify.rumble.effects.UseItemEffectHolder;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin implements UseItemEffectHolder {
    @Unique private ContinuousRumbleEffect useItemRumble;

    @Override
    protected void onStartUsingItem(InteractionHand hand, CallbackInfo ci, ItemStack stack) {
        switch (stack.getUseAnimation()) {
            case BOW -> startRumble(ContinuousRumbleEffect.builder()
                    .byTick(tick -> new RumbleState(
                            tick % 7 <= 3 && tick > BowItem.MAX_DRAW_DURATION ? 0.1f : 0f,
                            BowItem.getPowerForTime(tick)
                    ))
                    .build());
            case CROSSBOW -> {
                int chargeDuration = CrossbowItem.getChargeDuration(
                        stack
                        /*? if >1.20.6*//*,(LocalPlayer) (Object) this*/
                );
                startRumble(ContinuousRumbleEffect.builder()
                        .byTick(tick -> new RumbleState(
                                0f,
                                (float) tick / chargeDuration
                        ))
                        .timeout(chargeDuration)
                        .build());
            }
            case BLOCK, SPYGLASS -> startRumble(ContinuousRumbleEffect.builder()
                    .byTick(tick -> new RumbleState(
                            0f,
                            tick % 4 / 4f * 0.12f + 0.05f
                    ))
                    .build());
            case EAT, DRINK -> startRumble(ContinuousRumbleEffect.builder()
                    .constant(0.1f, 0.2f)
                    .build());
            case TOOT_HORN -> startRumble(ContinuousRumbleEffect.builder()
                    .byTick(tick -> new RumbleState(
                            Math.min(1f, tick / 10f),
                            0.25f
                    ))
                    .build());
            case SPEAR -> startRumble(ContinuousRumbleEffect.builder()
                    .constant(0.3f, 0.3f)
                    .build());
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

    @Unique
    private void startRumble(ContinuousRumbleEffect effect) {
        ControlifyApi.get().getCurrentController()
                .flatMap(ControllerEntity::rumble)
                .ifPresent(controller -> {
                    controller.rumbleManager().play(RumbleSource.INTERACTION, effect);
                    useItemRumble = effect;
                });
    }

    @Override
    public @Nullable ContinuousRumbleEffect controlify$getUseItemEffect() {
        return useItemRumble;
    }
}
