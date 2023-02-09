package dev.isxander.controlify.mixins.compat.screenop.vanilla;

import dev.isxander.controlify.screenop.component.CustomFocus;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractContainerEventHandler.class)
public abstract class AbstractContainerEventHandlerMixin implements CustomFocus {
    @Shadow public abstract @Nullable GuiEventListener getFocused();

    @Override
    public GuiEventListener getCustomFocus() {
        return getFocused();
    }
}
