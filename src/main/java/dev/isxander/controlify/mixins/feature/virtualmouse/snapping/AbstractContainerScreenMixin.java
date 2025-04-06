package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends ScreenMixin implements ISnapBehaviour {

    @Shadow public abstract T getMenu();

    @Shadow protected int leftPos;

    @Shadow protected int topPos;

    @Shadow protected int imageHeight;

    @Shadow
    @Final
    protected T menu;

    @Shadow
    protected int imageWidth;

    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        getMenu().slots.stream()
                .map(slot -> new SnapPoint(new Vector2i(leftPos + slot.x + 8, topPos + slot.y + 8), 17))
                .forEach(consumer);
    }
}
