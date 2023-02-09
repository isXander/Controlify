package dev.isxander.controlify.mixins.compat.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.SelectWorldScreenProcessor;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin implements ScreenProcessorProvider {
    private final SelectWorldScreenProcessor controlify$processor = new SelectWorldScreenProcessor((SelectWorldScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return controlify$processor;
    }
}
