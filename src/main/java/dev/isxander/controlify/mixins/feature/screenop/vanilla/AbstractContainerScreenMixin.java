package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.AbstractContainerScreenProcessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin implements ScreenProcessorProvider {
    @Shadow @Nullable protected Slot hoveredSlot;

    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int button, ClickType actionType);

    @Unique private final ScreenProcessor<?> screenProcessor = new AbstractContainerScreenProcessor<>((AbstractContainerScreen<?>) (Object) this, () -> hoveredSlot, this::slotClicked);

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return screenProcessor;
    }
}
