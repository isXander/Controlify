package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractFurnaceScreen.class)
public class AbstractFurnaceScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final RecipeBookScreenProcessor<AbstractFurnaceScreen> processor =
        new RecipeBookScreenProcessor<>((AbstractFurnaceScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
