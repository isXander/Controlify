package dev.isxander.controlify.mixins.compat.screen.vanilla;

import dev.isxander.controlify.compatibility.screen.ScreenProcessor;
import dev.isxander.controlify.compatibility.screen.ScreenProcessorProvider;
import dev.isxander.controlify.compatibility.vanilla.SelectWorldScreenProcessor;
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
