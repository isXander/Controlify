package dev.isxander.controlify.mixins.compat.screenop.vanilla;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker
    FocusNavigationEvent.ArrowNavigation invokeCreateArrowEvent(ScreenDirection direction);

    @Invoker
    void invokeChangeFocus(ComponentPath path);

    @Invoker
    void invokeClearFocus();
}
