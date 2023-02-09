package dev.isxander.controlify.mixins.compat.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.CreativeModeInventoryScreenProcessor;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin implements ScreenProcessorProvider {
    @Unique private final CreativeModeInventoryScreenProcessor controlify$screenProcessor
            = new CreativeModeInventoryScreenProcessor((CreativeModeInventoryScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$screenProcessor;
    }
}
