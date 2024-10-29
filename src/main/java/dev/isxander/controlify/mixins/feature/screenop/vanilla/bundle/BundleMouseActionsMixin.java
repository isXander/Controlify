package dev.isxander.controlify.mixins.feature.screenop.vanilla.bundle;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

//? if >=1.21.2 {
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.compat.vanilla.BundleItemSlotControllerAction;
import dev.isxander.controlify.screenop.compat.vanilla.ItemSlotControllerAction;
import net.minecraft.client.gui.BundleMouseActions;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BundleMouseActions.class)
public abstract class BundleMouseActionsMixin implements ItemSlotControllerAction {
    @Shadow
    protected abstract void toggleSelectedBundleItem(ItemStack itemStack, int i, int j);

    @Override
    public boolean controlify$onControllerInput(ItemStack stack, int hoveredSlotIndex, ControllerEntity controller) {
        return BundleItemSlotControllerAction.onControllerInput(stack, hoveredSlotIndex, controller, this::toggleSelectedBundleItem);
    }
}
//?} else {
/*@Mixin(Minecraft.class)
public class BundleMouseActionsMixin {}
*///?}
