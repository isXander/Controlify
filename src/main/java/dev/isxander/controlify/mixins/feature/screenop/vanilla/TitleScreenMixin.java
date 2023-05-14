package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.TitleScreenProcessor;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TitleScreen.class)
public class TitleScreenMixin implements ScreenProcessorProvider {
    @Unique private final ScreenProcessor<?> processor
            = new TitleScreenProcessor((TitleScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
