package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.ISnapBehaviour;
import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Screen.class)
public abstract class ScreenMixin implements ISnapBehaviour {
    @Shadow public abstract List<? extends GuiEventListener> children();

    @Shadow public int width;
    @Shadow public int height;
    @Final @Shadow protected @Nullable Minecraft minecraft;

    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        children().stream()
                .filter(child -> child instanceof AbstractWidget)
                .map(AbstractWidget.class::cast)
                .map(widget -> new SnapPoint(
                        new Vector2i(widget.getX() + widget.getWidth() / 2, widget.getY() + widget.getHeight() / 2),
                        Math.min(widget.getWidth(), widget.getHeight()) / 2 + 10
                ))
                .forEach(consumer);
    }
}
