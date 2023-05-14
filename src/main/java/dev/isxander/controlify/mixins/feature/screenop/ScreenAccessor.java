package dev.isxander.controlify.mixins.feature.screenop;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker
    FocusNavigationEvent.ArrowNavigation invokeCreateArrowEvent(ScreenDirection direction);

    @Invoker
    void invokeChangeFocus(ComponentPath path);

    @Invoker
    void invokeClearFocus();

    @Accessor
    List<Renderable> getRenderables();
}
