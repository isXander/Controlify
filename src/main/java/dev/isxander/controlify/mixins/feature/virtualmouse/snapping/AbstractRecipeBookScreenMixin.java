package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.SnapUtils;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

//? if >=1.21.2 {
@Mixin(AbstractRecipeBookScreen.class)
//?} else {
/*@Mixin(value = {
        InventoryScreen.class,
        AbstractFurnaceScreen.class,
        CraftingScreen.class
})
*///?}
public abstract class AbstractRecipeBookScreenMixin<T extends AbstractContainerMenu>
        extends AbstractContainerScreenMixin<T> {

    //? if >=1.21.2 {
    @Shadow @Final private RecipeBookComponent<?> recipeBookComponent;
    //?} else {
    /*@Shadow(remap = false, aliases = {"getRecipeBookComponent","m_5564_","method_2659"})
    public abstract RecipeBookComponent getRecipeBookComponent();
    *///?}

    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        SnapUtils.addRecipeSnapPoints(
                //? if >=1.21.2 {
                recipeBookComponent,
                //?} else {
                /*getRecipeBookComponent(),
                *///?}
                consumer
        );

        //? if >=1.21.2 {
        ScreenPosition recipeBookTogglePos = this.getRecipeBookButtonPosition();
        //?} else {
        /*ScreenPosition recipeBookTogglePos = new ScreenPosition(recipeBookToggleButton.getX(), recipeBookToggleButton.getY());
        *///?}
        consumer.accept(new SnapPoint(
                recipeBookTogglePos.x(),
                recipeBookTogglePos.y(),
                12
        ));
    }

    //? if >=1.21.2 {
    @Shadow
    protected abstract ScreenPosition getRecipeBookButtonPosition();
    //?} else {
    /*@Unique private ImageButton recipeBookToggleButton;

    // We need to capture the button and hold a reference to it
    // to add a snap point to it
    @ModifyExpressionValue(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "Lnet/minecraft/client/gui/components/ImageButton;"
            )
    )
    private ImageButton modifyRecipeBookToggleButton(ImageButton button) {
        this.recipeBookToggleButton = button;
        return button;
    }
    *///?}
}
