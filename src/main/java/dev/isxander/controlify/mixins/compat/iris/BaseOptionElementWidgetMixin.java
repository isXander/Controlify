package dev.isxander.controlify.mixins.compat.iris;

import dev.isxander.controlify.compatibility.iris.BaseOptionElementComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.BaseOptionElementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BaseOptionElementWidget.class, remap = false)
public abstract class BaseOptionElementWidgetMixin implements ComponentProcessorProvider {
    @Shadow public abstract boolean applyPreviousValue();
    @Shadow public abstract boolean applyNextValue();

    @Shadow protected NavigationController navigation;
    @Unique private final BaseOptionElementComponentProcessor processor
            = new BaseOptionElementComponentProcessor(this::cycle);

    @Override
    public ComponentProcessor componentProcessor() {
        return processor;
    }

    private void cycle(boolean reverse) {
        boolean needsUpdate = reverse ? applyPreviousValue() : applyNextValue();
        if (needsUpdate) {
            navigation.refresh();
        }
    }
}
