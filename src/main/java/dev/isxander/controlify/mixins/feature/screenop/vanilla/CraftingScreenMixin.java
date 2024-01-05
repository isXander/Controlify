package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CraftingScreen.class)
public class CraftingScreenMixin implements ScreenProcessorProvider {
    @Unique
    private final RecipeBookScreenProcessor<CraftingScreen> processor =
        new RecipeBookScreenProcessor<>((CraftingScreen) (Object) this);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
