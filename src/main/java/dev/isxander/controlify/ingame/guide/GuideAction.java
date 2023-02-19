package dev.isxander.controlify.ingame.guide;

import dev.isxander.controlify.bindings.ControllerBinding;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record GuideAction(ControllerBinding<?> binding, Component name, ActionLocation location,
                          ActionPriority priority) implements Comparable<GuideAction> {
    public GuideAction(ControllerBinding<?> binding, Component name, ActionLocation location) {
        this(binding, name, location, ActionPriority.NORMAL);
    }

    @Override
    public int compareTo(@NotNull GuideAction o) {
        return this.priority().compareTo(o.priority());
    }
}
