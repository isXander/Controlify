//? if iris {
/*package dev.isxander.controlify.compatibility.iris.screenop;

import dev.isxander.controlify.screenop.compat.AbstractSliderComponentProcessor;

import java.util.function.Consumer;

public class BaseOptionElementComponentProcessor extends AbstractSliderComponentProcessor {
    private final Consumer<Boolean> cycleMethod;

    public BaseOptionElementComponentProcessor(Consumer<Boolean> cycleMethod) {
        this.cycleMethod = cycleMethod;
    }

    @Override
    protected void incrementSlider(boolean reverse) {
        this.cycleMethod.accept(reverse);
    }
}
*///?}
