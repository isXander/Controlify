package dev.isxander.controlify.mixins.compat.screen.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.CreativeModeInventoryScreenProcessor;
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
