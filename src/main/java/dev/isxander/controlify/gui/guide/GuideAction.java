package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.bind.ControllerBinding;
import dev.isxander.controlify.api.guide.ActionPriority;
import dev.isxander.controlify.api.guide.GuideActionNameSupplier;
import org.jetbrains.annotations.NotNull;

public record GuideAction<T>(ControllerBinding binding, GuideActionNameSupplier<T> name, ActionPriority priority) implements Comparable<GuideAction<T>> {
    public GuideAction(ControllerBinding binding, GuideActionNameSupplier<T> name) {
        this(binding, name, ActionPriority.NORMAL);
    }

    @Override
    public int compareTo(@NotNull GuideAction<T> o) {
        return this.priority().compareTo(o.priority());
    }
}
