package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.AbstractSignEditScreenProcessor;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenMixin implements ScreenProcessorProvider {
    @Unique private final AbstractSignEditScreenProcessor screenProcessor =
            new AbstractSignEditScreenProcessor((AbstractSignEditScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }
}
