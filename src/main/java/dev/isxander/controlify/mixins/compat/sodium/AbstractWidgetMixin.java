package dev.isxander.controlify.mixins.compat.sodium;

import me.jellysquid.mods.sodium.client.gui.widgets.AbstractWidget;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractWidget.class)
public abstract class AbstractWidgetMixin implements GuiEventListener {
    @Shadow protected abstract void drawRect(double x1, double y1, double x2, double y2, int color);

    @Shadow protected abstract void playClickSound();

    @Shadow public abstract boolean isFocused();

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return !this.isFocused() ? ComponentPath.leaf(this) : null;
    }
}
