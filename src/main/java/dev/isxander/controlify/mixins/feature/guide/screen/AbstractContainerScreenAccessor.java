package dev.isxander.controlify.mixins.feature.guide.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor
    Slot getHoveredSlot();

    @Invoker
    boolean invokeHasClickedOutside(double mouseX, double mouseY, int left, int top, int button);

    @Accessor
    int getLeftPos();

    @Accessor
    int getTopPos();
}
