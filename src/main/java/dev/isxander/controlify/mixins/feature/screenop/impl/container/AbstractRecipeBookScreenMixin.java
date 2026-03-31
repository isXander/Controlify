package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin
        extends AbstractContainerScreenMixin
        implements ScreenProcessorProvider, RecipeUpdateListener, RecipeBookScreenProcessor.RecipeBookScreenAccessor {

    @Unique
    private final RecipeBookScreenProcessor<?> processor =
            new RecipeBookScreenProcessor<>(
                    (AbstractRecipeBookScreen<?>) (Object) this,
                    this,
                    () -> hoveredSlot,
                    this::slotClicked,
                    this::handleControllerItemSlotActions
            );

    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Unique
    private RecipeBookComponent<?> getRecipeBookComponent() {
        return recipeBookComponent;
    }

    @Override
    public RecipeBookComponent<?> controlify$getRecipeBookComponent() {
        return this.getRecipeBookComponent();
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
