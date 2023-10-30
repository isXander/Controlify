package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements ISnapBehaviour {

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Shadow public abstract T getMenu();

    @Shadow protected int leftPos;

    @Shadow protected int topPos;

    @Shadow protected int imageHeight;

    @Override
    public Set<SnapPoint> getSnapPoints() {
        return getMenu().slots.stream()
                .map(slot -> new SnapPoint(new Vector2i(leftPos + slot.x + 8, topPos + slot.y + 8), 15))
                .collect(Collectors.toSet());
    }
}
