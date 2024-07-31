//? if iris {
package dev.isxander.controlify.compatibility.iris.mixins;

import dev.isxander.controlify.compatibility.iris.screenop.BaseOptionElementComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessor;
import dev.isxander.controlify.screenop.ComponentProcessorProvider;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.element.widget.BaseOptionElementWidget;
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

    @Unique
    private void cycle(boolean reverse) {
        boolean needsUpdate = reverse ? applyPreviousValue() : applyNextValue();
        if (needsUpdate) {
            navigation.refresh();
        }
    }

}
//?}
