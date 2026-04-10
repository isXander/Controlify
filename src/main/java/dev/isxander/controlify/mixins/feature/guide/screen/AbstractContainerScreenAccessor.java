package dev.isxander.controlify.mixins.feature.guide.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot controlify$getHoveredSlot();

    @Invoker("hasClickedOutside")
    boolean controlify$invokeHasClickedOutside(double mouseX, double mouseY, int left, int top);

    @Accessor("leftPos")
    int controlify$getLeftPos();

    @Accessor("topPos")
    int controlify$getTopPos();
}
