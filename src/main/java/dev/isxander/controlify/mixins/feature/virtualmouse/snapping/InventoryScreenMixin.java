package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.SnapUtils;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreenMixin<InventoryMenu> implements ISnapBehaviour {
    @Shadow @Final private RecipeBookComponent recipeBookComponent;

    protected InventoryScreenMixin(Component title) {
        super(title);
    }

    @Override
    public Set<SnapPoint> getSnapPoints() {
        Set<SnapPoint> points = new HashSet<>(super.getSnapPoints());
        SnapUtils.addRecipeSnapPoints(recipeBookComponent, points);
        return points;
    }
}
