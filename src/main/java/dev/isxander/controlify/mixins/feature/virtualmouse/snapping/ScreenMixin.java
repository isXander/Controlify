package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.virtualmouse.ISnapBehaviour;
import dev.isxander.controlify.virtualmouse.SnapPoint;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ISnapBehaviour {
    @Shadow public abstract List<? extends GuiEventListener> children();

    @Override
    public Set<SnapPoint> getSnapPoints() {
        return children().stream()
                .filter(child -> child instanceof AbstractWidget)
                .map(AbstractWidget.class::cast)
                .map(widget -> new SnapPoint(
                        new Vector2i(widget.getX() + widget.getWidth() / 2, widget.getY() + widget.getHeight() / 2),
                        Math.min(widget.getWidth(), widget.getHeight()) / 2 + 10
                ))
                .collect(Collectors.toSet());
    }
}
