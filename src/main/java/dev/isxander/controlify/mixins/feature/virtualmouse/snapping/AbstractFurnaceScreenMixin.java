package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.SnapUtils;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.recipebook.AbstractFurnaceRecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

@Mixin(AbstractFurnaceScreen.class)
public abstract class AbstractFurnaceScreenMixin<T extends AbstractFurnaceMenu> extends AbstractContainerScreenMixin<T> {
    @Shadow @Final public AbstractFurnaceRecipeBookComponent recipeBookComponent;

    protected AbstractFurnaceScreenMixin(Component title) {
        super(title);
    }

    @Override
    public Set<SnapPoint> getSnapPoints() {
        Set<SnapPoint> points = new HashSet<>(super.getSnapPoints());
        SnapUtils.addRecipeSnapPoints(recipeBookComponent, points);
        return points;
    }
}
