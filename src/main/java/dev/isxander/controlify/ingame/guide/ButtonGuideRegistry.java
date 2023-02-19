package dev.isxander.controlify.ingame.guide;

import dev.isxander.controlify.bindings.ControllerBinding;

public interface ButtonGuideRegistry {
    void registerGuideAction(ControllerBinding<?> binding, ActionLocation location, GuideActionNameSupplier supplier);
}
